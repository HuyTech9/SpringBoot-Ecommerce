package com.brian.ecommerce.project.service;
import org.springframework.data.domain.Pageable;

import com.brian.ecommerce.project.exceptions.APIException;
import com.brian.ecommerce.project.exceptions.ResourceNotFoundException;
import com.brian.ecommerce.project.model.Category;
import com.brian.ecommerce.project.model.Product;
import com.brian.ecommerce.project.payload.ProductDTO;
import com.brian.ecommerce.project.payload.ProductResponse;
import com.brian.ecommerce.project.repositories.CategoryRepository;
import com.brian.ecommerce.project.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;

    @Value("${project.image}")
    private String path;



    @Transactional
    @Override
    public ProductDTO addProduct(Long categoryId, ProductDTO productDTO){

        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
        // Check if product already present or not (last step)
        boolean ifProductNotPresent = true;

        List<Product> products = category.getProducts();
        for (Product value : products) {
            if (value.getProductName().equals(productDTO.getProductName())) {
                ifProductNotPresent = false;
                break;
            }
        }

        if (ifProductNotPresent) {
            Product product = modelMapper.map(productDTO, Product.class);
            product.setImage("default.png");
            product.setCategory(category);
            double specialPrice = product.getPrice()-((product.getDiscount() * 0.01) * product.getPrice());
            product.setSpecialPrice(specialPrice);

            // Saved the product into the database
            Product savedProduct = productRepository.save(product);
            // Convert the saved product into DTO and return it
            return modelMapper.map(savedProduct, ProductDTO.class);
        } else {
            throw new APIException("Product already exist!!");
        }
    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder;
        if (sortOrder.equalsIgnoreCase("asc")) {
            sortByAndOrder = Sort.by(sortBy).ascending();
        } else {
            sortByAndOrder = Sort.by(sortBy).descending();
        }

        Pageable pageDetail = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Page<Product> pageProducts = productRepository.findAll(pageDetail);
        // Create an empty list to store the mapped ProductDTO objects
        List<ProductDTO> productDTOS = new ArrayList<>();

        // Iterate through the list of products
        for (Product product : pageProducts.getContent()) {
            // Convert each Product entity to a ProductDTO using modelMapper
            ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);
            // Add the mapped ProductDTO to the list
            productDTOS.add(productDTO);
        }
        if (productDTOS.isEmpty()){
            throw new APIException("No products found!");
        }

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());


        return productResponse;
    }

    @Override
    public ProductResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        // product size is 0

        // Retrieve the category by its ID; if not found, throw a ResourceNotFoundException
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        Sort sortByAndOrder;
        if (sortOrder.equalsIgnoreCase("asc")) {
            sortByAndOrder = Sort.by(sortBy).ascending();
        } else {
            sortByAndOrder = Sort.by(sortBy).descending();
        }

        Pageable pageDetail = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> pageProducts = productRepository.findByCategoryOrderByPriceAsc(category, pageDetail);
        List<ProductDTO> productDTOS = new ArrayList<>();
        for (Product product : pageProducts) {
            ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);
            productDTOS.add(productDTO);
        }
        if (pageProducts.getSize() == 0){
            throw new APIException(category.getCategoryName() + "category does not have any products!");
        }
        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());

        return productResponse;
    }

    @Override
    public ProductResponse searchProductByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        Sort sortByAndOrder;
        if (sortOrder.equalsIgnoreCase("asc")) {
            sortByAndOrder = Sort.by(sortBy).ascending();
        } else {
            sortByAndOrder = Sort.by(sortBy).descending();
        }
        Pageable pageDetail = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> pageProducts = productRepository.findByProductNameLikeIgnoreCase('%'+keyword+'%', pageDetail);

        List<ProductDTO> productDTOS = new ArrayList<>();
        // Iterate through the list of products
        for (Product product : pageProducts) {
            // Convert each Product entity to a ProductDTO using modelMapper
            ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);
            // Add the mapped ProductDTO to the list
            productDTOS.add(productDTO);
        }

        if (pageProducts.getSize() == 0){
            throw new APIException("No products found!");
        }

        // Create a new ProductResponse object
        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());

        return productResponse;
    }


    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
        // Get the existing product from DB
        Product productFromDb = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        Product product = modelMapper.map(productDTO, Product.class);

        // Update the product info with user shared
        productFromDb.setProductName(product.getProductName());
        productFromDb.setDescription(product.getDescription());
        productFromDb.setQuantity(product.getQuantity());
        productFromDb.setDiscount(product.getDiscount());
        productFromDb.setPrice(product.getPrice());
        productFromDb.setSpecialPrice(product.getSpecialPrice());

        // Saved to database
        Product savedProduct = productRepository.save(productFromDb);
        // Save to database
        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        productRepository.delete(product);
        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        // Get the product from DB
        Product productFromDb = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        // Upload image to server
        // Get the file name of uploaded image
        String fileName = fileService.uploadImage(path, image);

        // Updating the new file name to the product
        productFromDb.setImage(fileName);

        // Save updated product
        Product updatedProduct = productRepository.save(productFromDb);


        // Return DTO after mapping product to DTO
        return modelMapper.map(updatedProduct, ProductDTO.class);


    }

}



