package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {
    @Autowired
    private CourseCategoryMapper courseCategoryMapper;
    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {

        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes(id);

        Map<String, CourseCategoryTreeDto> mapTemp = courseCategoryTreeDtos.stream()
                .filter(i -> !id.equals(i.getId()))
                .collect(Collectors.toMap(key -> key.getId(), value -> value,(key1,key2)->key2));

        List<CourseCategoryTreeDto> categoryDtoList = new ArrayList<CourseCategoryTreeDto>();

        courseCategoryTreeDtos.stream()
                .filter(i -> !id.equals(i.getId()))
                .forEach(i ->{
                    if (i.getParentid().equals(id)){
                        categoryDtoList.add(i);
                    }
                    CourseCategoryTreeDto courseCategoryParent = mapTemp.get(i.getParentid());
                    if (courseCategoryParent!=null){
                        if (courseCategoryParent.getChildrenTreeNodes()==null){
                            courseCategoryParent.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                        }
                        courseCategoryParent.getChildrenTreeNodes().add(i);
                    }
                });

        return categoryDtoList;
    }
}
