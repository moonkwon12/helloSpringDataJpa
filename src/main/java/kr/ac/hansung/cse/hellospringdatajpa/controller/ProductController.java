package kr.ac.hansung.cse.hellospringdatajpa.controller;

import kr.ac.hansung.cse.hellospringdatajpa.entity.Product;
import kr.ac.hansung.cse.hellospringdatajpa.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService service;

    @GetMapping({"", "/"})
    public String viewHomePage(Model model, HttpServletRequest request) {
        List<Product> listProducts = service.listAll();
        model.addAttribute("listProducts", listProducts);

        // 로그인 성공 메시지 처리
        String loginMessage = (String) request.getSession().getAttribute("loginMessage");
        if (loginMessage != null) {
            model.addAttribute("message", loginMessage);
            request.getSession().removeAttribute("loginMessage");
        }

        return "products/index";  // 현재 구조에 맞춤
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String showNewProductPage(Model model) {
        Product product = new Product();
        model.addAttribute("product", product);
        return "products/new_product";  // 현재 구조에 맞춤
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditProductPage(@PathVariable(name = "id") Long id, Model model) {
        try {
            Product product = service.get(id);
            model.addAttribute("product", product);
            return "products/edit_product";  // 현재 구조에 맞춤
        } catch (Exception e) {
            return "redirect:/products";
        }
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveProduct(@Valid @ModelAttribute("product") Product product,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {

        // 추가 유효성 검사 (Bean Validation과 함께)
        boolean hasErrors = false;

        // 상품명 검증
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            hasErrors = true;
        } else if (product.getName().trim().length() < 2) {
            result.rejectValue("name", "error.product", "상품명은 최소 2글자 이상이어야 합니다.");
            hasErrors = true;
        }

        // 브랜드 검증
        if (product.getBrand() == null || product.getBrand().trim().isEmpty()) {
            hasErrors = true;
        }

        // 가격 검증
        if (product.getPrice() < 0) {
            result.rejectValue("price", "error.product", "가격은 0 이상이어야 합니다.");
            hasErrors = true;
        } else if (product.getPrice() > 10000000) {
            result.rejectValue("price", "error.product", "가격은 1,000만원 이하여야 합니다.");
            hasErrors = true;
        }

        // Bean Validation 오류 또는 커스텀 검증 오류가 있으면 폼으로 돌아가기
        if (result.hasErrors() || hasErrors) {
            if (product.getId() != null) {
                return "products/edit_product";  // 현재 구조에 맞춤
            } else {
                return "products/new_product";   // 현재 구조에 맞춤
            }
        }

        try {
            // 데이터 정리 (trim)
            product.setName(product.getName().trim());
            product.setBrand(product.getBrand().trim());
            if (product.getMadeIn() != null) {
                product.setMadeIn(product.getMadeIn().trim());
            }

            service.save(product);

            if (product.getId() != null) {
                redirectAttributes.addFlashAttribute("message", "상품이 성공적으로 수정되었습니다.");
            } else {
                redirectAttributes.addFlashAttribute("message", "상품이 성공적으로 등록되었습니다.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "상품 저장 중 오류가 발생했습니다: " + e.getMessage());
        }

        return "redirect:/products";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteProduct(@PathVariable(name = "id") Long id, RedirectAttributes redirectAttributes) {
        try {
            service.delete(id);
            redirectAttributes.addFlashAttribute("message", "상품이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "상품 삭제 중 오류가 발생했습니다.");
        }
        return "redirect:/products";
    }
}