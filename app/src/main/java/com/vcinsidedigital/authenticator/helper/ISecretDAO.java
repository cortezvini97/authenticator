package com.vcinsidedigital.authenticator.helper;

import android.content.Context;

import com.vcinsidedigital.authenticator.model.Secret;

import java.util.List;

public interface ISecretDAO
{
    public boolean save(Secret secret);
    public boolean update(Secret secret);
    public boolean delete(Secret secret);
    public List<Secret> listAll();
}
