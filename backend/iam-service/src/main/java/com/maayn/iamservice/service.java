package com.maayn.iamservice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import maayn.veld.generated.errors.GetIamException;
import maayn.veld.generated.models.*;
import maayn.veld.generated.services.IIamService;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class service implements IIamService{

    @Override
    public List<IamItem> listIam() {
        return List.of();
    }

    @Override
    public IamItem getIam(String id) throws GetIamException {
        return null;
    }

    @Override
    public IamItem createIam(CreateIamInput input) {
        return null;
    }
}