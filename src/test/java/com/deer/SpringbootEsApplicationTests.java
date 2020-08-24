package com.deer;

import com.deer.pojo.Item;
import com.deer.service.ItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SpringBootTest(classes = SpringbootEsApplication.class)
class SpringbootEsApplicationTests {


    @Autowired
    private ItemService itemService;


    @Test
    void deleteIndex() {

        itemService.deleteIndex();
    }

    @Test
    public void creatIndex(){
        boolean b = itemService.addIndex();
        System.out.println("创建库是否成功"+b);
    }

    @Test
    void findMapping() {

        Map<String, Object> mapper = itemService.findMapper();
        System.out.println(mapper);
    }

    @Test
    void addDoc() {

        List<Item> list = new ArrayList<>();
        list.add(new Item(1L, "小米手机7", "手机", "小米", 3299.00, "http://image.leyou.com/13123.jpg"));
        list.add(new Item(2L, "坚果手机R1", "手机", "锤子", 3699.00, "http://image.leyou.com/13123.jpg"));
        list.add(new Item(3L, "华为META10", "手机", "华为", 4499.00, "http://image.leyou.com/13123.jpg"));
        list.add(new Item(4L, "小米Mix2S", "手机", "小米", 4299.00, "http://image.leyou.com/13123.jpg"));
        list.add(new Item(5L, "荣耀V10", "手机", "华为", 2799.00, "http://image.leyou.com/13123.jpg"));

        int i= 0;

        for (Item item : list) {
            itemService.addDoc(item);
            i++;
        }
        System.out.println("创建了"+i+"个文档");


//        itemService.addDoc();
    }

    @Test
    void searchDoc(){
        AggregatedPage<Item> items = itemService.highLightQuery("小米");

        for (Item item : items) {
            System.out.println(item);
        }

    }

}
