package com.shop.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // 기존 데이터 모두 삭제 후 새로 추가
        productRepository.deleteAll();
        
        // 저렴한 샘플 상품 데이터 추가
        productRepository.save(new Product("기본 흰색 티셔츠", "심플한 면 티셔츠", 5000, 100, "basic-tshirt.jpg", "상의"));
        productRepository.save(new Product("면 양말 3켤레", "편안한 면 양말 세트", 3000, 200, "cotton-socks.jpg", "속옷"));
        productRepository.save(new Product("무지 맨투맨", "기본 무지 맨투맨", 8000, 80, "plain-sweatshirt.jpg", "상의"));
        productRepository.save(new Product("데님 청바지", "클래식 스트레이트 핏", 12000, 60, "denim-jeans.jpg", "하의"));
        productRepository.save(new Product("스니커즈", "데일리 운동화", 15000, 50, "sneakers.jpg", "신발"));
        productRepository.save(new Product("볼캡", "심플 볼캡", 4000, 150, "ball-cap.jpg", "액세서리"));
        productRepository.save(new Product("조거팬츠", "편안한 조거팬츠", 9000, 70, "jogger-pants.jpg", "하의"));
        productRepository.save(new Product("후드티", "기본 후드티", 13000, 40, "hoodie-tee.jpg", "상의"));
        productRepository.save(new Product("크로스백", "미니 크로스백", 7000, 90, "cross-bag.jpg", "가방"));
        productRepository.save(new Product("슬리퍼", "실내용 슬리퍼", 2000, 120, "slippers.jpg", "신발"));
        productRepository.save(new Product("반팔 셔츠", "여름용 반팔 셔츠", 6000, 110, "short-sleeve-shirt.jpg", "상의"));
        productRepository.save(new Product("레깅스", "스포츠 레깅스", 8500, 65, "leggings.jpg", "하의"));
        productRepository.save(new Product("아이패드 케이스", "태블릿 보호케이스", 4500, 85, "tablet-case.jpg", "액세서리"));
        productRepository.save(new Product("니트 스웨터", "따뜻한 니트", 11000, 45, "knit-sweater.jpg", "상의"));
        productRepository.save(new Product("트렌치코트", "가을 트렌치코트", 18000, 30, "trench-coat.jpg", "아우터"));
        
        System.out.println("✅ 저렴한 상품 데이터 초기화 완료!");
    }
}
