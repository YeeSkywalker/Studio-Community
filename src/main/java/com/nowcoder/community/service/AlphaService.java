package com.nowcoder.community.service;

import com.nowcoder.community.dao.AlphaDao;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
//@Scope("prototype")
public class AlphaService {

    @Autowired
    private AlphaDao alphaDao;

    public AlphaService(){
        System.out.println("classify Alpha Service");
    }
    @PostConstruct
    public void init() {
        System.out.println("init Alpha Service");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("destroy Alpha Service");
    }

    public String find() {
        return alphaDao.select();
    }
}
