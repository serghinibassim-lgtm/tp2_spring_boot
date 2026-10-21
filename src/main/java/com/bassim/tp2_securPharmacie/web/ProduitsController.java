package com.bassim.tp2_securPharmacie.web;

import com.bassim.tp2_securPharmacie.dtos.CategorieDto;
import com.bassim.tp2_securPharmacie.dtos.FournisseurDto;
import com.bassim.tp2_securPharmacie.dtos.ProduitDto;
import com.bassim.tp2_securPharmacie.dtos.TagDto;
import com.bassim.tp2_securPharmacie.mappers.ProduitMappers;
import com.bassim.tp2_securPharmacie.models.Produit;
import com.bassim.tp2_securPharmacie.repositories.CatgorieRepository;
import com.bassim.tp2_securPharmacie.repositories.FournisseurRepository;
import com.bassim.tp2_securPharmacie.repositories.ProduitRepository;
import com.bassim.tp2_securPharmacie.repositories.TagRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class ProduitsController {

    ProduitRepository produitRepository;
    ProduitMappers produitMappers;

    private final CatgorieRepository catgorieRepository;
    private final FournisseurRepository fournisseurRepository;
    private final TagRepository tagRepository;


    @GetMapping("/api/produits")
    public List<ProduitDto> ListToutLesProduits() {
        List<Produit> toutLesProduits = produitRepository.findAll();
        List<ProduitDto> toutLesProduitDtos = toutLesProduits.stream().map(Prod -> {
            return produitMappers.fromProduits(Prod);
        }).collect(Collectors.toList());
        return toutLesProduitDtos;
    }


    @GetMapping("/api/produits/{produit_id}")
    public ProduitDto produitbyid(@PathVariable Long produit_id) {
        Produit produit = produitRepository.findById(produit_id).orElse(null);
        return produitMappers.fromProduits(produit);

    }

    @PutMapping("/api/produits/{produit_id}")
    public ProduitDto modifiProduit(@PathVariable Long produit_id, @RequestBody ProduitDto newProd) {
        Produit oldProduit = produitRepository.findById(produit_id).orElseThrow(() ->
                new RuntimeException("Produit introuvable  :" + produit_id));
        ProduitDto oldProduitDTO = produitMappers.fromProduits(oldProduit);
        produitMappers.copyDTO(newProd, oldProduitDTO);

        Produit produitModifier = produitMappers.toProduit(oldProduitDTO);
        produitModifier.setId(produit_id);
        Produit savedProduit = produitRepository.save(produitModifier);
        return produitMappers.fromProduits(savedProduit);
    }

    @PostMapping("/api/produits")
    public ProduitDto creerProduit(@RequestBody ProduitDto dto) {
        Produit produit = produitMappers.toProduit(dto);
        Produit saved = produitRepository.save(produit);
        return produitMappers.fromProduits(saved);
    }

    @DeleteMapping("/api/produits/{id_prod}")
    public String deletProduct(@PathVariable Long id_prod) {
        Produit produit = produitRepository.findById(id_prod).orElseThrow(() ->
                new RuntimeException("ce Produit et introuvable " + id_prod));
        produitRepository.delete(produit);
        return "prod id:" + id_prod + " et suprimer";
    }

    @GetMapping("/api/categories")
    public List<CategorieDto> listeCategories() {
        return catgorieRepository.findAll().stream().map(categorie -> {
            CategorieDto dto = new CategorieDto();
            BeanUtils.copyProperties(categorie, dto);
            return dto;
        }).toList();
    }

    @GetMapping("/api/fournisseurs")
    public List<FournisseurDto> listeFournisseurs() {
        return fournisseurRepository.findAll().stream().map(fournisseur -> {
            FournisseurDto dto = new FournisseurDto();
            BeanUtils.copyProperties(fournisseur, dto);
            return dto;
        }).toList();
    }

    @GetMapping("/api/tags")
    public List<TagDto> listeTags() {
        return tagRepository.findAll().stream().map(tag -> {
            TagDto dto = new TagDto();
            BeanUtils.copyProperties(tag, dto);
            return dto;
        }).toList();
    }
}

