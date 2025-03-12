package com.vcinsidedigital.authenticator.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vcinsidedigital.authenticator.R;
import com.vcinsidedigital.authenticator.activities.CodeGenActivity;
import com.vcinsidedigital.authenticator.activities.MainActivity;
import com.vcinsidedigital.authenticator.model.Secret;
import com.vcinsidedigital.authenticator.util.ResourceUtil;

import java.util.List;

public class SecretAdapter extends RecyclerView.Adapter<SecretAdapter.MyViewHolder>
{
    private List<Secret> secretList;
    private Context context;

    public SecretAdapter(List<Secret> secretList, Context context)
    {
        this.secretList = secretList;
        this.context = context;
    }


    @NonNull
    @Override
    public SecretAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.secret_adapter, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull SecretAdapter.MyViewHolder holder, int position) {
        Secret secret = secretList.get(position);
        String name = secret.getName();
        Secret secretNew = secret.createIssuer();
        String accountName = secretNew.getAccountName();
        String issuer = secretNew.getIssuer();


        if(name.contains(":")){
            holder.textViewName.setText(issuer); // Parte antes do ":"
            holder.textViewDetalhes.setText(accountName); // Parte depois do ":"
        }else {
            holder.textViewName.setText(name); // Parte antes do ":"
            holder.textViewDetalhes.setText(name);
        }

        if (ResourceUtil.getIcon(issuer) != 0){
            holder.imageView.setVisibility(View.VISIBLE);
            holder.imageView.setImageResource(ResourceUtil.getIcon(issuer));
        }else {
            holder.textViewIcon.setVisibility(View.VISIBLE);
            holder.textViewIcon.setText(issuer.substring(0,2).toUpperCase());
        }
    }

    @Override
    public int getItemCount() {
        return this.secretList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView textViewName, textViewDetalhes, textViewIcon;
        ImageView imageView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.text_view_name);
            textViewDetalhes = itemView.findViewById(R.id.text_view_detalhes);
            textViewIcon = itemView.findViewById(R.id.avatarText);
            imageView = itemView.findViewById(R.id.image_icon);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Secret secret = secretList.get(getAdapterPosition());
                    Intent i = new Intent(context, CodeGenActivity.class);
                    i.putExtra("secret", secret);
                    context.startActivity(i);
                }
            });
        }
    }
}
