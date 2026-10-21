package com.bassim.tp2_securPharmacie.mappers;

import com.bassim.tp2_securPharmacie.dtos.CategorieDto;
import com.bassim.tp2_securPharmacie.dtos.DetailProduitDto;
import com.bassim.tp2_securPharmacie.dtos.FournisseurDto;
import com.bassim.tp2_securPharmacie.dtos.ProduitDto;
import com.bassim.tp2_securPharmacie.dtos.TagDto;
import com.bassim.tp2_securPharmacie.models.Categorie;
import com.bassim.tp2_securPharmacie.models.DetailProduit;
import com.bassim.tp2_securPharmacie.models.Fournisseure;
import com.bassim.tp2_securPharmacie.models.Produit;
import com.bassim.tp2_securPharmacie.models.Tag;
import com.bassim.tp2_securPharmacie.repositories.CatgorieRepository;
import com.bassim.tp2_securPharmacie.repositories.FournisseurRepository;
import com.bassim.tp2_securPharmacie.repositories.TagRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProduitMappers {

    private final CatgorieRepository catgorieRepository;
    private final FournisseurRepository fournisseurRepository;
    private final TagRepository tagRepository;

    public ProduitDto fromProduits(Produit produit) {
        ProduitDto dto = new ProduitDto();
        BeanUtils.copyProperties(produit, dto, "categorie", "fournisseur", "detailProduit", "tags");
        CategorieDto catDto = new CategorieDto();
        catDto.setId(produit.getCategorie().getId());
        BeanUtils.copyProperties(produit.getCategorie(), catDto);
        dto.setCategorie(catDto);
        FournisseurDto fDto = new FournisseurDto();
        BeanUtils.copyProperties(produit.getFournisseur(), fDto);
        dto.setFournisseur(fDto);
        DetailProduitDto detailDto = new DetailProduitDto();
        BeanUtils.copyProperties(produit.getDetailProduit(), detailDto);
        dto.setDetailProduit(detailDto);
        List<TagDto> tagDtos = produit.getTags().stream().map(t -> {
            TagDto td = new TagDto();
            BeanUtils.copyProperties(t, td);
            return td;
        }).distinct().collect(Collectors.toList());
        dto.setTags(tagDtos);
        return dto;
    }

    public Produit toProduit(ProduitDto dto) {
        Produit produit = new Produit();
        BeanUtils.copyProperties(dto, produit, "id", "categorie", "fournisseur", "detailProduit", "tags");
        Categorie categorie = catgorieRepository.getReferenceById(dto.getCategorie().getId());
        produit.setCategorie(categorie);
        Fournisseure f = fournisseurRepository.getReferenceById(dto.getFournisseur().getId());
        produit.setFournisseur(f);
        DetailProduit detail = new DetailProduit();
        BeanUtils.copyProperties(dto.getDetailProduit(), detail);
        produit.setDetailProduit(detail);
        List<Long> ids = dto.getTags().stream().map(TagDto::getId).distinct().collect(Collectors.toList());
        List<Tag> tags = tagRepository.findAllById(ids);
        produit.setTags(tags);
        return produit;
    }

    public void copyDTO(ProduitDto prodSource,ProduitDto prodTarget){

        BeanUtils.copyProperties(prodSource,prodTarget);

    }

}
