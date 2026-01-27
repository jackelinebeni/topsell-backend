package com.topsell.backend.loader;

import com.topsell.backend.entity.*;
import com.topsell.backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final BannerRepository bannerRepository;

    // Inyección de dependencias por constructor
    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder,
                      ProductRepository productRepository,
                      CategoryRepository categoryRepository,
                      BrandRepository brandRepository,
                      BannerRepository bannerRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.bannerRepository = bannerRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Solo insertamos datos si la BD está vacía (para no duplicar al reiniciar)
        if (categoryRepository.count() == 0) {
            loadData();
        }
        if (userRepository.findByEmail("admin@topsell.com").isEmpty()) {

            User admin = User.builder()
                    .firstName("Super")
                    .lastName("Admin")
                    .email("admin@topsell.com")
                    .password(passwordEncoder.encode("admin123")) // Contraseña inicial
                    .phone("999999999")
                    .role(Role.ADMIN) // IMPORTANTE: Rol Admin
                    .build();

            userRepository.save(admin);
            System.out.println("✅ USUARIO ADMIN CREADO: admin@topsell.com / admin123");
        }
    }

    private void loadData() {
        // 1. CREAR MARCAS
        Brand nike = new Brand();
        nike.setName("Nike");
        nike.setSlug("nike");
        nike.setLogoUrl("https://placehold.co/100x100?text=Nike");

        Brand adidas = new Brand();
        adidas.setName("Adidas");
        adidas.setSlug("adidas");
        adidas.setLogoUrl("https://placehold.co/100x100?text=Adidas");

        Brand samsung = new Brand();
        samsung.setName("Samsung");
        samsung.setSlug("samsung");
        samsung.setLogoUrl("https://placehold.co/100x100?text=Samsung");

        brandRepository.saveAll(Arrays.asList(nike, adidas, samsung));

        // 2. CREAR CATEGORÍAS Y SUBCATEGORÍAS
        // Ropa
        Category ropa = new Category();
        ropa.setName("Ropa");
        ropa.setSlug("ropa");
        ropa.setImage("https://placehold.co/300x300?text=Ropa");

        SubCategory camisetas = new SubCategory();
        camisetas.setName("Camisetas");
        camisetas.setSlug("camisetas");
        camisetas.setCategory(ropa);

        SubCategory pantalones = new SubCategory();
        pantalones.setName("Pantalones");
        pantalones.setSlug("pantalones");
        pantalones.setCategory(ropa);

        ropa.setSubCategories(Arrays.asList(camisetas, pantalones));

        // Tecnología
        Category tecno = new Category();
        tecno.setName("Tecnología");
        tecno.setSlug("tecnologia");
        tecno.setImage("https://placehold.co/300x300?text=Tecno");

        SubCategory laptops = new SubCategory();
        laptops.setName("Laptops");
        laptops.setSlug("laptops");
        laptops.setCategory(tecno);

        SubCategory celulares = new SubCategory();
        celulares.setName("Celulares");
        celulares.setSlug("celulares");
        celulares.setCategory(tecno);

        tecno.setSubCategories(Arrays.asList(laptops, celulares));

        // Guardamos las categorías (por el CascadeType.ALL, se guardan solas las subcategorías)
        categoryRepository.saveAll(Arrays.asList(ropa, tecno));

        // 3. CREAR BANNERS (HERO CAROUSEL)
        Banner banner1 = new Banner();
        banner1.setTitle("Ofertas de Verano");
        banner1.setImageUrl("https://placehold.co/1200x500?text=Ofertas+Verano");
        banner1.setTargetUrl("/categoria/ropa");
        banner1.setSortOrder(1);
        banner1.setActive(true);

        Banner banner2 = new Banner();
        banner2.setTitle("Nueva Colección Tech");
        banner2.setImageUrl("https://placehold.co/1200x500?text=Tecnologia+2026");
        banner2.setTargetUrl("/categoria/tecnologia");
        banner2.setSortOrder(2);
        banner2.setActive(true);

        bannerRepository.saveAll(Arrays.asList(banner1, banner2));

        // 4. CREAR PRODUCTOS
        Product p1 = new Product();
        p1.setName("Zapatillas Air Max");
        p1.setSlug("zapatillas-air-max");
        p1.setShortDescription("Comodidad máxima para correr");
        p1.setLongDescription("Estas zapatillas cuentan con tecnología de aire comprimido...");
        p1.setPrice(new BigDecimal("120.50"));
        p1.setStock(50);
        p1.setImageUrl("https://placehold.co/600x600?text=Zapatillas+Nike");
        p1.setCategory(ropa);
        p1.setBrand(nike);

        Product p2 = new Product();
        p2.setName("Polera Originals");
        p2.setSlug("polera-originals");
        p2.setShortDescription("Estilo clásico urbano");
        p2.setLongDescription("Algodón 100% peruano con diseño exclusivo.");
        p2.setPrice(new BigDecimal("65.00"));
        p2.setStock(20);
        p2.setImageUrl("https://placehold.co/600x600?text=Polera+Adidas");
        p2.setCategory(ropa);
        p2.setBrand(adidas);

        Product p3 = new Product();
        p3.setName("Galaxy S25");
        p3.setSlug("galaxy-s25");
        p3.setShortDescription("El futuro en tus manos");
        p3.setLongDescription("Pantalla AMOLED 8K, procesador cuántico...");
        p3.setPrice(new BigDecimal("999.99"));
        p3.setStock(10);
        p3.setImageUrl("https://placehold.co/600x600?text=Galaxy+S25");
        p3.setCategory(tecno);
        p3.setBrand(samsung);

        // Creamos más productos para rellenar carouseles
        Product p4 = new Product();
        p4.setName("Monitor Curvo 34");
        p4.setSlug("monitor-curvo-34");
        p4.setShortDescription("Inmersión total para gaming");
        p4.setLongDescription("Tasa de refresco de 240Hz.");
        p4.setPrice(new BigDecimal("450.00"));
        p4.setStock(5);
        p4.setImageUrl("https://placehold.co/600x600?text=Monitor+Samsung");
        p4.setCategory(tecno);
        p4.setBrand(samsung);

        productRepository.saveAll(Arrays.asList(p1, p2, p3, p4));

        System.out.println("--- DATA SEEDER TERMINADO: BASE DE DATOS POBLADA ---");
    }
}