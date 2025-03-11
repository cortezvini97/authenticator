package com.vcinsidedigital.authenticator.model;

import android.content.Context;

import com.vcinsidedigital.authenticator.helper.SecretDAO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

public class Secret implements Serializable
{
    private Long id;
    private String name;
    private String accountName;
    private String code;
    private String type;
    private String issuer;
    private URI uri;

    public Secret()
    {

    }

    public Secret(Long id, String name, String code, String type) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean save(Context context){
        try{
            if(this.id != null){
                SecretDAO dao = new SecretDAO(context);
                return dao.update(this);
            }else {
                SecretDAO dao = new SecretDAO(context);
                return dao.save(this);
            }
        }catch (Exception e){
            return false;
        }
    }

    public byte[] getData() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(this);
        out.flush();
        byte[] serializedData = bos.toByteArray();
        return serializedData;
    }


    public Secret createIssuer() {
        if(this.name.contains(":")){
            String[] parts = this.name.split(":");
            this.issuer = parts[0];
            this.accountName = parts[1];
        }else {
            this.issuer = name;
        }
        return this;
    }

    public String getIssuer() {
        return this.issuer;
    }

    public String getAccountName(){
        return this.accountName;
    }


    public void createURI(){
        try {
            this.uri = new URI("otpauth://totp/"+issuer+":"+name+"?secret="+code+"&issuer="+issuer);
        } catch (URISyntaxException e) {
            this.uri = null;
        }
    }

    public URI getUri() {
        return uri;
    }
}
