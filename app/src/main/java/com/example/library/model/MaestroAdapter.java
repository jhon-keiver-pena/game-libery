package com.example.library.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.gamelibery.R;
import com.example.library.model.rest.Maestro;

import java.util.List;

public class MaestroAdapter extends RecyclerView.Adapter<MaestroAdapter.MaestroViewHolder> {
    private List<Maestro> maestros;
    private Context context;

    public MaestroAdapter(List<Maestro> maestros, Context context) {
        this.maestros = maestros;
        this.context = context;
    }

    @NonNull
    @Override
    public MaestroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_maestro, parent, false);
        return new MaestroViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MaestroViewHolder holder, int position) {
        Maestro maestro = maestros.get(position);
        holder.textNombre.setText(maestro.getNombre());
        holder.textTelefono.setText(maestro.getTelefono());
        holder.textEdad.setText(maestro.getEdad());
        holder.textSexo.setText(maestro.getSexo());
        holder.textExperiencia.setText(maestro.getExperiencia());
        holder.textTiempoCampo.setText(maestro.getTiempoCampo());
        holder.textEspecialidad.setText(maestro.getEspecialidad());

        // Cargar la imagen usando Glide
        Glide.with(context)
                .load(maestro.getUrlImagen())
                .transition(DrawableTransitionOptions.withCrossFade()) // Efecto de transición
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return maestros.size();
    }

    public static class MaestroViewHolder extends RecyclerView.ViewHolder {
        TextView textNombre, textTelefono, textEdad, textSexo, textExperiencia, textTiempoCampo, textEspecialidad;
        ImageView imageView;

        public MaestroViewHolder(@NonNull View itemView) {
            super(itemView);
            textNombre = itemView.findViewById(R.id.textNombre);
            textTelefono = itemView.findViewById(R.id.textTelefono);
            textEdad = itemView.findViewById(R.id.textEdad);
            textSexo = itemView.findViewById(R.id.textSexo);
            textExperiencia = itemView.findViewById(R.id.textExperiencia);
            textTiempoCampo = itemView.findViewById(R.id.textTiempoCampo);
            textEspecialidad = itemView.findViewById(R.id.textEspecialidad);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
