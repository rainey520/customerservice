package com.ruoyi.project.erp.purchase.controller;

import java.util.List;

import com.ruoyi.common.exception.BusinessException;
import com.ruoyi.common.utils.poi.ExcelUtils;
import com.ruoyi.common.utils.security.ShiroUtils;
import com.ruoyi.framework.jwt.JwtUtil;
import com.ruoyi.project.device.devCompany.service.IDevCompanyService;
import com.ruoyi.project.erp.contract.service.IContractService;
import com.ruoyi.project.erp.mrp.domain.Mrp;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.aspectj.weaver.loadtime.Aj;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.project.erp.purchase.domain.Purchase;
import com.ruoyi.project.erp.purchase.service.IPurchaseService;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.common.utils.poi.ExcelUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * 采购单 信息操作处理
 * 
 * @author zqm
 * @date 2019-05-10
 */
@Controller
@RequestMapping("/erp/purchase")
public class PurchaseController extends BaseController
{
    private String prefix = "erp/purchase";
	
	@Autowired
	private IPurchaseService purchaseService;

	@Autowired
	private IDevCompanyService companyService;

	@Autowired
	private IContractService contractService;
	
	@RequiresPermissions("erp:purchase:view")
	@GetMapping()
	public String purchase()
	{
	    return prefix + "/purchase";
	}
	
	/**
	 * 查询采购单列表
	 */
	@RequiresPermissions("erp:purchase:list")
	@PostMapping("/list")
	@ResponseBody
	public TableDataInfo list(Purchase purchase,HttpServletRequest request)
	{
		startPage();
        List<Purchase> list = purchaseService.selectPurchaseList(purchase,request);
		return getDataTable(list);
	}
	
	
	/**
	 * 导出采购单列表
	 */
	@RequiresPermissions("erp:purchase:export")
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(Purchase purchase,HttpServletRequest request)
    {
    	List<Purchase> list = purchaseService.selectPurchaseList(purchase,request);
        ExcelUtil<Purchase> util = new ExcelUtil<Purchase>(Purchase.class);
        return util.exportExcel(list, "purchase");
    }
	
	/**
	 * 新增采购单
	 */
	@GetMapping("/add")
	public String add()
	{
	    return prefix + "/add";
	}

	@GetMapping("/addPur")
	public String addPur(Integer supplierId, ModelMap modelMap){
		modelMap.put("supplierId",supplierId);
		return prefix + "/checkTime";
	}
	
	/**
	 * 新增保存采购单
	 */
	@RequiresPermissions("erp:purchase:add")
	@Log(title = "采购单", businessType = BusinessType.INSERT)
	@PostMapping("/add")
	@ResponseBody
	public AjaxResult addSave(@RequestBody Purchase purchase,HttpServletRequest request)
	{
		return toAjax(purchaseService.insertPurchase(purchase,request));
	}

	/**
	 * 修改采购单
	 */
	@GetMapping("/edit/{id}")
	public String edit(@PathVariable("id") Integer id, ModelMap mmap)
	{
		Purchase purchase = purchaseService.selectPurchaseById(id);
		mmap.put("purchase", purchase);
	    return prefix + "/edit";
	}
	
	/**
	 * 修改保存采购单
	 */
	@RequiresPermissions("erp:purchase:edit")
	@Log(title = "采购单", businessType = BusinessType.UPDATE)
	@PostMapping("/edit")
	@ResponseBody
	public AjaxResult editSave(@RequestBody Purchase purchase)
	{
		return toAjax(purchaseService.updatePurchase(purchase));
	}
	
	/**
	 * 删除采购单
	 */
	@RequiresPermissions("erp:purchase:remove")
	@Log(title = "采购单", businessType = BusinessType.DELETE)
	@PostMapping( "/remove")
	@ResponseBody
	public AjaxResult remove(String ids)
	{		
		return toAjax(purchaseService.deletePurchaseByIds(ids));
	}


	/**
	 * 操作采购状态
	 * @param purchase 采购单对象
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/status")
	@RequiresPermissions("erp:purchase:edit")
	public AjaxResult editStatus(Purchase purchase){
		try {
			return toAjax(purchaseService.editStatus(purchase));
		} catch (BusinessException e) {
			return error(e.getMessage());
		}
	}

	/**
	 * 采购单详情
	 * @param id 采购单id
	 * @return
	 */
	@RequestMapping("/detail")
	@RequiresPermissions("erp:purchase:details")
	public String detailsPurchase(int id, ModelMap mmap, HttpServletRequest request){
		mmap.put("purchase",purchaseService.selectPurchaseById(id));
		mmap.put("company",companyService.selectDevCompanyById(JwtUtil.getTokenUser(request).getCompanyId()));
		mmap.put("contract",contractService.selectContractByCompanyId(request));
		return prefix +"/details";
	}

	/**
	 * 查询存在预收库存的采购单
	 * @param supplierId 供应商id
	 * @return 结果
	 */
	@PostMapping("/purchaseHavePreNumberBySupId")
	@ResponseBody
	public AjaxResult purchaseHavePreNumberBySupId(Integer supplierId,HttpServletRequest request){
		List<Purchase> purchaseList = purchaseService.selectPurchaseHavePreNumberBySupId(supplierId,request);
		return AjaxResult.success("success",purchaseList);
	}

	/**
	 * 关闭采购单
	 * @param purchase 采购单
	 * @return 结果
	 */
	@RequiresPermissions("erp:purchase:close")
	@PostMapping("/closePurchase")
	@ResponseBody
	public AjaxResult closePurchase(Purchase purchase){
		try {
			return toAjax(purchaseService.closePurchase(purchase));
		} catch (BusinessException e) {
			return error(e.getMessage());
		}
	}

	/**
	 * 导出采购单明细
	 * @param id
	 * @return
	 */
	@PostMapping("/uploadPurchase")
	@ResponseBody
	public AjaxResult uploadPurchase(Integer id,HttpServletRequest request){
		Workbook wb = purchaseService.uploadPurchase(id,request);
		String fileName = ExcelUtils.encodingFilename("采购单");
		return ExcelUtils.getAjaxResult(wb,fileName);
	}

	/**
	 * 勾选mrp转化成采购单
	 */
	@PostMapping("/mrpToPurchase")
	@ResponseBody
	public AjaxResult mrpToPurchase(@RequestBody Purchase purchase){
		try {
			return toAjax(purchaseService.mrpToPurchase(purchase));
		} catch (BusinessException e) {
			return error(e.getMessage());
		}
	}
}
