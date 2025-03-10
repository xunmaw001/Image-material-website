











package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.*;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;
import com.alibaba.fastjson.*;

/**
 * 高清背景
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/beijing")
public class BeijingController {
    private static final Logger logger = LoggerFactory.getLogger(BeijingController.class);

    @Autowired
    private BeijingService beijingService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;

    //级联表service
    @Autowired
    private YonghuService yonghuService;



    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(StringUtil.isEmpty(role))
            return R.error(511,"权限为空");
        else if("用户".equals(role))
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        params.put("beijingDeleteStart",1);params.put("beijingDeleteEnd",1);
        if(params.get("orderBy")==null || params.get("orderBy")==""){
            params.put("orderBy","id");
        }
        PageUtils page = beijingService.queryPage(params);

        //字典表数据转换
        List<BeijingView> list =(List<BeijingView>)page.getList();
        for(BeijingView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        BeijingEntity beijing = beijingService.selectById(id);
        if(beijing !=null){
            //entity转view
            BeijingView view = new BeijingView();
            BeanUtils.copyProperties( beijing , view );//把实体数据重构到view中

                //级联表
                YonghuEntity yonghu = yonghuService.selectById(beijing.getYonghuId());
                if(yonghu != null){
                    BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setYonghuId(yonghu.getId());
                }
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody BeijingEntity beijing, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,beijing:{}",this.getClass().getName(),beijing.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(StringUtil.isEmpty(role))
            return R.error(511,"权限为空");
        else if("用户".equals(role)){
            beijing.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
            beijing.setShangxiaTypes(2);
        }
        Wrapper<BeijingEntity> queryWrapper = new EntityWrapper<BeijingEntity>()
            .eq("yonghu_id", beijing.getYonghuId())
            .eq("beijing_name", beijing.getBeijingName())
            .eq("beijing_types", beijing.getBeijingTypes())
            .eq("beijing_geshi_types", beijing.getBeijingGeshiTypes())
            .eq("beijing_banshi_types", beijing.getBeijingBanshiTypes())
            .eq("shangxia_types", beijing.getShangxiaTypes())
            .eq("beijing_delete", beijing.getBeijingDelete())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        BeijingEntity beijingEntity = beijingService.selectOne(queryWrapper);
        if(beijingEntity==null){
            beijing.setBeijingDelete(1);
            beijing.setCreateTime(new Date());
            beijingService.insert(beijing);
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody BeijingEntity beijing, HttpServletRequest request){
        logger.debug("update方法:,,Controller:{},,beijing:{}",this.getClass().getName(),beijing.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(StringUtil.isEmpty(role))
            return R.error(511,"权限为空");
        else if("用户".equals(role))
            beijing.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
        //根据字段查询是否有相同数据
        Wrapper<BeijingEntity> queryWrapper = new EntityWrapper<BeijingEntity>()
            .notIn("id",beijing.getId())
            .andNew()
            .eq("yonghu_id", beijing.getYonghuId())
            .eq("beijing_name", beijing.getBeijingName())
            .eq("beijing_types", beijing.getBeijingTypes())
            .eq("beijing_geshi_types", beijing.getBeijingGeshiTypes())
            .eq("beijing_banshi_types", beijing.getBeijingBanshiTypes())
            .eq("shangxia_types", beijing.getShangxiaTypes())
            .eq("beijing_delete", beijing.getBeijingDelete())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        BeijingEntity beijingEntity = beijingService.selectOne(queryWrapper);
        if("".equals(beijing.getBeijingPhoto()) || "null".equals(beijing.getBeijingPhoto())){
                beijing.setBeijingPhoto(null);
        }
        if("".equals(beijing.getBeijingFile()) || "null".equals(beijing.getBeijingFile())){
                beijing.setBeijingFile(null);
        }
        if(beijingEntity==null){
            //  String role = String.valueOf(request.getSession().getAttribute("role"));
            //  if("".equals(role)){
            //      beijing.set
            //  }
            beijingService.updateById(beijing);//根据id更新
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        ArrayList<BeijingEntity> list = new ArrayList<>();
        for(Integer id:ids){
            BeijingEntity beijingEntity = new BeijingEntity();
            beijingEntity.setId(id);
            beijingEntity.setBeijingDelete(2);
            list.add(beijingEntity);
        }
        if(list != null && list.size() >0){
            beijingService.updateBatchById(list);
        }
        return R.ok();
    }

    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        try {
            List<BeijingEntity> beijingList = new ArrayList<>();//上传的东西
            Map<String, List<String>> seachFields= new HashMap<>();//要查询的字段
            Date date = new Date();
            int lastIndexOf = fileName.lastIndexOf(".");
            if(lastIndexOf == -1){
                return R.error(511,"该文件没有后缀");
            }else{
                String suffix = fileName.substring(lastIndexOf);
                if(!".xls".equals(suffix)){
                    return R.error(511,"只支持后缀为xls的excel文件");
                }else{
                    URL resource = this.getClass().getClassLoader().getResource("static/upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if(!file.exists()){
                        return R.error(511,"找不到上传文件，请联系管理员");
                    }else{
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for(List<String> data:dataList){
                            //循环
                            BeijingEntity beijingEntity = new BeijingEntity();
//                            beijingEntity.setYonghuId(Integer.valueOf(data.get(0)));   //用户 要改的
//                            beijingEntity.setBeijingName(data.get(0));                    //高清背景名称 要改的
//                            beijingEntity.setBeijingPhoto("");//照片
//                            beijingEntity.setBeijingTypes(Integer.valueOf(data.get(0)));   //高清背景分类 要改的
//                            beijingEntity.setBeijingGeshiTypes(Integer.valueOf(data.get(0)));   //高清背景格式 要改的
//                            beijingEntity.setBeijingBanshiTypes(Integer.valueOf(data.get(0)));   //板式 要改的
//                            beijingEntity.setBeijingFile(data.get(0));                    //压缩包 要改的
//                            beijingEntity.setShangxiaTypes(Integer.valueOf(data.get(0)));   //是否上架 要改的
//                            beijingEntity.setBeijingDelete(1);//逻辑删除字段
//                            beijingEntity.setBeijingContent("");//照片
//                            beijingEntity.setCreateTime(date);//时间
                            beijingList.add(beijingEntity);


                            //把要查询是否重复的字段放入map中
                        }

                        //查询是否重复
                        beijingService.insertBatch(beijingList);
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }




    /**
    * 前端列表
    */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("list方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));

        // 没有指定排序字段就默认id倒序
        if(StringUtil.isEmpty(String.valueOf(params.get("orderBy")))){
            params.put("orderBy","id");
        }
        PageUtils page = beijingService.queryPage(params);

        //字典表数据转换
        List<BeijingView> list =(List<BeijingView>)page.getList();
        for(BeijingView c:list)
            dictionaryService.dictionaryConvert(c, request); //修改对应字典表字段
        return R.ok().put("data", page);
    }

    /**
    * 前端详情
    */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("detail方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        BeijingEntity beijing = beijingService.selectById(id);
            if(beijing !=null){
                //entity转view
                BeijingView view = new BeijingView();
                BeanUtils.copyProperties( beijing , view );//把实体数据重构到view中

                //级联表
                    YonghuEntity yonghu = yonghuService.selectById(beijing.getYonghuId());
                if(yonghu != null){
                    BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setYonghuId(yonghu.getId());
                }
                //修改对应字典表字段
                dictionaryService.dictionaryConvert(view, request);
                return R.ok().put("data", view);
            }else {
                return R.error(511,"查不到数据");
            }
    }


    /**
    * 前端保存
    */
    @RequestMapping("/add")
    public R add(@RequestBody BeijingEntity beijing, HttpServletRequest request){
        logger.debug("add方法:,,Controller:{},,beijing:{}",this.getClass().getName(),beijing.toString());
        Wrapper<BeijingEntity> queryWrapper = new EntityWrapper<BeijingEntity>()
            .eq("yonghu_id", beijing.getYonghuId())
            .eq("beijing_name", beijing.getBeijingName())
            .eq("beijing_types", beijing.getBeijingTypes())
            .eq("beijing_geshi_types", beijing.getBeijingGeshiTypes())
            .eq("beijing_banshi_types", beijing.getBeijingBanshiTypes())
            .eq("shangxia_types", beijing.getShangxiaTypes())
            .eq("beijing_delete", beijing.getBeijingDelete())
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        BeijingEntity beijingEntity = beijingService.selectOne(queryWrapper);
        if(beijingEntity==null){
            beijing.setBeijingDelete(1);
            beijing.setCreateTime(new Date());
        //  String role = String.valueOf(request.getSession().getAttribute("role"));
        //  if("".equals(role)){
        //      beijing.set
        //  }
        beijingService.insert(beijing);
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }



}
