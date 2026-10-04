package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String CACHE_SHOP_TYPE_KEY = "cache:shopType";

    public Result queryTypeList(){
        String key = CACHE_SHOP_TYPE_KEY;
        List<String> shopTypeJsons = stringRedisTemplate.opsForList().range(key, 0, -1);
        //判断redis中有无缓存
        if (shopTypeJsons != null && !shopTypeJsons.isEmpty()) {
            //如果有缓存，直接返回缓存数据
            List<ShopType> shopList = new ArrayList<>();
            for (String shopType1:shopTypeJsons){
                shopList.add(JSONUtil.toBean(shopType1,ShopType.class));
            }
            return Result.ok(shopList);
        }
        //如果redis里面没有缓存，查数据库
        List<ShopType> shopList = query().orderByAsc("sort").list();
        if (shopList == null || shopList.isEmpty()) {
            return Result.fail("店铺分类不存在！");
        }
        //将结果写入Redis
        List<String> jsonList = shopList.stream().map(JSONUtil::toJsonStr)
                .collect(Collectors.toList());;
        stringRedisTemplate.opsForList().rightPushAll(key,jsonList);

        return Result.ok(shopList);
    }
}
