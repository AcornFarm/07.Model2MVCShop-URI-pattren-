package com.model2.mvc.web.purchase;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.model2.mvc.common.Page;
import com.model2.mvc.common.Search;
import com.model2.mvc.service.domain.Product;
import com.model2.mvc.service.domain.Purchase;
import com.model2.mvc.service.domain.User;
import com.model2.mvc.service.product.ProductService;
import com.model2.mvc.service.purchase.PurchaseService;



//==> 회원관리 Controller
@Controller
@RequestMapping("/purchase/*")
public class PurchaseController {
	
	///Field
	@Autowired
	@Qualifier("purchaseServiceImpl")
	private PurchaseService purchaseService;
	//setter Method 구현 않음
	
	@Autowired
	@Qualifier("productServiceImpl")
	private ProductService productService;
		
	public PurchaseController(){
		System.out.println(this.getClass());
	}
	
	//==> classpath:config/common.properties  ,  classpath:config/commonservice.xml 참조 할것
	//==> 아래의 두개를 주석을 풀어 의미를 확인 할것
	@Value("#{commonProperties['pageUnit']}")
	//@Value("#{commonProperties['pageUnit'] ?: 3}")
	int pageUnit;
	
	@Value("#{commonProperties['pageSize']}")
	//@Value("#{commonProperties['pageSize'] ?: 2}")
	int pageSize;
	
	
	@RequestMapping(value="addPurchase", method=RequestMethod.GET)
	public ModelAndView addPurchaseView(@RequestParam("prodNo") int prodNo, HttpSession session) throws Exception {

		System.out.println("addPurchase : GET");
		
		Product product = productService.getProduct(prodNo);
		User user = (User)session.getAttribute("user");
		System.out.println(user);
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("uvo", user);
		modelAndView.addObject("pvo", product);
		modelAndView.setViewName("forward:/purchase/addPurchaseView.jsp");
		
		return modelAndView;
	}
	
	
	  @RequestMapping(value="addPurchase", method=RequestMethod.POST) 
	  public ModelAndView addPurchase( @ModelAttribute("purchase") Purchase purchase, @RequestParam("buyerId") String buyerId,
																					@RequestParam("prodNo") int prodNo) throws Exception{
	  
	  System.out.println("addPurchase : POST"); //Business Logic
	  
	  User user = new User();
	  user.setUserId(buyerId);
	  
	  Product product = new Product();
	  product.setProdNo(prodNo);
	  
	  purchase.setBuyer(user);
	  purchase.setPurchaseProd(product);
	  purchaseService.addPurchase(purchase);
	  
	  System.out.println("::"+purchase);
	  
	  Purchase puvo = purchaseService.getPurchase2(prodNo);
	  
	  System.out.println("::::"+puvo);
	  
	  return new ModelAndView("forward:/purchase/addPurchase.jsp", "purchase", puvo); 
	  
	  }
	 
	
	
	  @RequestMapping(value="getPurchase", method=RequestMethod.GET) 
	  public ModelAndView getPurchase( @RequestParam("tranNo") int tranNo ) throws Exception {
	  
	  System.out.println("getPurchase : GET"); //Business Logic Product product =
	  System.out.println(tranNo);
	  
	  Purchase purchase = purchaseService.getPurchase(tranNo);
	  System.out.println(purchase);
	  String viewName="forward:/purchase/getPurchase.jsp";
	  
	  return new ModelAndView(viewName, "purchase", purchase); 
	  
	  }
	  
	  
	
	  @RequestMapping(value="updatePurchase", method=RequestMethod.GET) 
	  public ModelAndView updatePurchaseView( @RequestParam("tranNo") int tranNo ) throws Exception{
	  
	  System.out.println("updatePurchase : GET"); //Business Logic Product product
	  Purchase purchase = purchaseService.getPurchase(tranNo); // Model 과 View 연결
	  
	  return  new ModelAndView("forward:/purchase/updatePurchaseView.jsp", "purchase", purchase);
	  
	  }
	  
	  
	  @RequestMapping(value="updatePurchase", method = RequestMethod.POST) 
	  public ModelAndView updatePurchase( @ModelAttribute("purchase") Purchase purchase , @RequestParam("buyerId") String buyerId
			  													) throws Exception{
	  
	  System.out.println("updatePurchase : POST"); //Business Logic
	  User user = new User();
	  user.setUserId(buyerId);
	  
	  purchase.setBuyer(user);
	  System.out.println(":::::"+purchase);
	 
	  purchaseService.updatePurchase(purchase);
	  Purchase puvo = purchaseService.getPurchase(purchase.getTranNo());
	  
	  return new ModelAndView("forward:/purchase/getPurchase.jsp", "purchase", puvo); 
	  
	  }
	  
	  
	  @RequestMapping(value="updateTranCode") 
	  public ModelAndView updateTranCode( @RequestParam("prodNo") int prodNo, @RequestParam("tranCode") String tranCode, HttpSession session
			  													) throws Exception{
	  
	  System.out.println("updateTranCode"); //Business Logic
	  
	  System.out.println(prodNo+"::::"+tranCode);
	  
	  Product product = new Product();
	  product.setProdNo(prodNo);
	  Purchase purchase = new Purchase();
	  purchase.setPurchaseProd(product);
	  purchase.setTranCode(tranCode);
	  
	  System.out.println(":::::"+purchase);
	 
	  purchaseService.updateTranCode(purchase);
	  
	  ModelAndView modelAndView = new ModelAndView();
	  User user = (User)session.getAttribute("user");
	  
	  if(user.getRole().contains("user")) {
		  modelAndView.setViewName("forward:/purchase/listPurchase");
	  }else {
		  modelAndView.setViewName("forward:/product/listProduct");
	  }
	  
	  return modelAndView;
	  
	  }
	  
	  
	
	  @RequestMapping(value="listPurchase") 
	  public ModelAndView listPurchase( @ModelAttribute("search") Search search, 
	  																	HttpSession session) throws Exception{
	  
	  System.out.println("listPurchase");
	  
	  if(search.getCurrentPage() ==0 ){ search.setCurrentPage(1); }
	  search.setPageSize(pageSize);
	  
	  // Business logic 수행 Map<String , Object>
	  String buyerId = ((User)session.getAttribute("user")).getUserId();
	  Map<String, Object> map = new HashMap<String, Object>();
	  map=purchaseService.getPurchaseList(search, buyerId);
	  
	  Page resultPage = new Page( search.getCurrentPage(),
	  ((Integer)map.get("totalCount")).intValue(), pageUnit, pageSize);
	  System.out.println(resultPage);
	  
	  ModelAndView modelAndView = new ModelAndView();
	  modelAndView.setViewName("forward:/purchase/listPurchase.jsp");
	  modelAndView.addObject("resultPage", resultPage); 
	  modelAndView.addObject("list", map.get("list"));
	  modelAndView.addObject("search",search);
	  
	  return modelAndView; 
	  }
	 
	  
	 
	 
}