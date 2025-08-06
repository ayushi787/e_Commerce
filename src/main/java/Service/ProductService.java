package Service;

import Repository.ProductRepository;
import entity.Product;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

public class ProductService {
    @Service
    public class ProductService {

        @Autowired
        private ProductRepository productRepository;

        // Fetch all products
        public List<Product> getAllProducts() {
            return productRepository.findAll();
        }

        // Fetch product by ID
        public Optional<Product> getProductById(Long id) {
            return productRepository.findById(id);
        }

        // Add or update product
        public Product saveProduct(Product product) {
            return productRepository.save(product);
        }

        // Delete product
        public void deleteProduct(Long id) {
            productRepository.deleteById(id);
        }
    }
}
