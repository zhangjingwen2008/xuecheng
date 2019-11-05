package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsPageRepositoryTest {

    @Autowired
    CmsPageRepository cmsPageRepository;

    @Test
    public void testFindAllByExample(){
        int page=0;
        int size=10;
        Pageable pageable = new PageRequest(page, size);

        CmsPage cms = new CmsPage();
//        cms.setPageId("adsf");
        cms.setPageAliase("轮播");
        ExampleMatcher matcher = ExampleMatcher.matching();
        matcher = matcher.withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());

        Example<CmsPage> example = Example.of(cms,matcher);
        Page<CmsPage> list = cmsPageRepository.findAll(example, pageable);
        List<CmsPage> all = list.getContent();
        System.out.println(all);
    }

    @Test
    public void testFindPage(){
        int page=0;
        int size=10;
        Pageable pageable = PageRequest.of(page, size);
        Page<CmsPage> all = cmsPageRepository.findAll(pageable);
        System.out.println(all);
    }
}
