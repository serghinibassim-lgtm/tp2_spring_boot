package com.bassim.tp2_securPharmacie.mappers;

import com.bassim.tp2_securPharmacie.dtos.CategorieDto;
import com.bassim.tp2_securPharmacie.dtos.DetailProduitDto;
import com.bassim.tp2_securPharmacie.dtos.FournisseurDto;
import com.bassim.tp2_securPharmacie.dtos.ProduitDto;
import com.bassim.tp2_securPharmacie.dtos.TagDto;
import com.bassim.tp2_securPharmacie.models.DetailProduit;
import com.bassim.tp2_securPharmacie.models.Produit;
import com.bassim.tp2_securPharmacie.repositories.CatgorieRepository;
import com.bassim.tp2_securPharmacie.repositories.FournisseurRepository;
import com.bassim.tp2_securPharmacie.repositories.TagRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class ProduitMappers {

    private final CatgorieRepository catgorieRepository;
    private final FournisseurRepository fournisseurRepository;
    private final TagRepository tagRepository;

    public ProduitDto fromProduits(Produit produit) {
        if (produit == null) {
            return null;
        }

        ProduitDto dto = new ProduitDto();
        BeanUtils.copyProperties(produit, dto, "categorie", "fournisseur", "detailProduit", "tags");
        dto.setStock(produit.getStock() == null ? 0 : produit.getStock());

        if (produit.getCategorie() != null) {
            CategorieDto catDto = new CategorieDto();
            BeanUtils.copyProperties(produit.getCategorie(), catDto);
            dto.setCategorie(catDto);
        }

        if (produit.getFournisseur() != null) {
            FournisseurDto fDto = new FournisseurDto();
            BeanUtils.copyProperties(produit.getFournisseur(), fDto);
            dto.setFournisseur(fDto);
        }

        if (produit.getDetailProduit() != null) {
            DetailProduitDto detailDto = new DetailProduitDto();
            BeanUtils.copyProperties(produit.getDetailProduit(), detailDto);
            dto.setDetailProduit(detailDto);
        }

        if (produit.getTags() == null) {
            dto.setTags(List.of());
        } else {
            dto.setTags(produit.getTags().stream().map(tag -> {
                TagDto tagDto = new TagDto();
                BeanUtils.copyProperties(tag, tagDto);
                return tagDto;
            }).distinct().toList());
        }

        return dto;
    }

    public Produit toProduit(ProduitDto dto) {
        Produit produit = new Produit();
        applyDto(dto, produit);
        return produit;
    }

    public void applyDto(ProduitDto dto, Produit produit) {
        produit.setNom(dto.getNom());
        produit.setDescription(dto.getDescription());
        produit.setPrix(dto.getPrix());
        produit.setCodeBarre(dto.getCodeBarre());
        produit.setDateExpiration(dto.getDateExpiration());

        if (dto.getStock() != null) {
            produit.setStock(dto.getStock());
        } else if (produit.getStock() == null) {
            produit.setStock(0);
        }

        if (dto.getCategorie() != null && dto.getCategorie().getId() != null) {
            produit.setCategorie(
                    catgorieRepository.getReferenceById(dto.getCategorie().getId()));
        }

        if (dto.getFournisseur() != null && dto.getFournisseur().getId() != null) {
            produit.setFournisseur(
                    fournisseurRepository.getReferenceById(dto.getFournisseur().getId()));
        }

        if (dto.getDetailProduit() != null) {
            DetailProduit detail = produit.getDetailProduit();
            if (detail == null) {
                detail = new DetailProduit();
            }
            BeanUtils.copyProperties(dto.getDetailProduit(), detail);
            produit.setDetailProduit(detail);
        }

        if (dto.getTags() == null) {
            produit.setTags(new ArrayList<>());
        } else {
            List<Long> ids = dto.getTags().stream()
                    .map(TagDto::getId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            produit.setTags(new ArrayList<>(tagRepository.findAllById(ids)));
        }
    }
}
