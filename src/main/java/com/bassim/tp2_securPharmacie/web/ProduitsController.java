package com.bassim.tp2_securPharmacie.web;

import com.bassim.tp2_securPharmacie.dtos.CategorieDto;
import com.bassim.tp2_securPharmacie.dtos.FournisseurDto;
import com.bassim.tp2_securPharmacie.dtos.ProduitDto;
import com.bassim.tp2_securPharmacie.dtos.StockUpdateRequest;
import com.bassim.tp2_securPharmacie.dtos.TagDto;
import com.bassim.tp2_securPharmacie.mappers.ProduitMappers;
import com.bassim.tp2_securPharmacie.models.Categorie;
import com.bassim.tp2_securPharmacie.models.Fournisseure;
import com.bassim.tp2_securPharmacie.models.Produit;
import com.bassim.tp2_securPharmacie.repositories.CatgorieRepository;
import com.bassim.tp2_securPharmacie.repositories.FournisseurRepository;
import com.bassim.tp2_securPharmacie.repositories.ProduitRepository;
import com.bassim.tp2_securPharmacie.repositories.TagRepository;
import com.bassim.tp2_securPharmacie.repositories.VenteRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@AllArgsConstructor
public class ProduitsController {

    private final ProduitRepository produitRepository;
    private final ProduitMappers produitMappers;
    private final CatgorieRepository catgorieRepository;
    private final FournisseurRepository fournisseurRepository;
    private final TagRepository tagRepository;
    private final VenteRepository venteRepository;

    @GetMapping("/api/produits")
    public List<ProduitDto> listToutLesProduits() {
        return produitRepository.findAll().stream()
                .map(produitMappers::fromProduits)
                .toList();
    }

    @GetMapping("/api/produits/{produit_id}")
    public ProduitDto produitById(@PathVariable Long produit_id) {
        Produit produit = produitRepository.findById(produit_id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Produit introuvable : " + produit_id));
        return produitMappers.fromProduits(produit);
    }

    @PutMapping("/api/produits/{produit_id}")
    public ProduitDto modifierProduit(@PathVariable Long produit_id,
                                      @RequestBody ProduitDto newProduit) {
        Produit produit = produitRepository.findById(produit_id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Produit introuvable : " + produit_id));

        produitMappers.applyDto(newProduit, produit);
        return produitMappers.fromProduits(produitRepository.save(produit));
    }

    @PostMapping("/api/produits")
    public ProduitDto creerProduit(@RequestBody ProduitDto dto) {
        Produit saved = produitRepository.save(produitMappers.toProduit(dto));
        return produitMappers.fromProduits(saved);
    }

    @PatchMapping("/api/produits/{id}/stock")
    public ProduitDto modifierStock(@PathVariable Long id,
                                    @RequestBody StockUpdateRequest request) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Produit introuvable : " + id));

        if (request == null || request.stock() == null || request.stock() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le stock doit être un entier positif ou nul.");
        }

        produit.setStock(request.stock());
        return produitMappers.fromProduits(produitRepository.save(produit));
    }

    @DeleteMapping("/api/produits/{id_prod}")
    public String supprimerProduit(@PathVariable Long id_prod) {
        Produit produit = produitRepository.findById(id_prod)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Produit introuvable : " + id_prod));

        if (venteRepository.existsByProduitId(id_prod)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Impossible de supprimer un produit déjà vendu.");
        }

        produitRepository.delete(produit);
        return "Produit " + id_prod + " supprimé.";
    }

    @GetMapping("/api/categories")
    public List<CategorieDto> listeCategories() {
        return catgorieRepository.findAll().stream().map(categorie -> {
            CategorieDto dto = new CategorieDto();
            BeanUtils.copyProperties(categorie, dto);
            return dto;
        }).toList();
    }

    @GetMapping("/api/categories/{id}")
    public CategorieDto categorieById(@PathVariable Long id) {
        Categorie categorie = catgorieRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Catégorie introuvable : " + id));
        CategorieDto dto = new CategorieDto();
        BeanUtils.copyProperties(categorie, dto);
        return dto;
    }

    @GetMapping("/api/fournisseurs")
    public List<FournisseurDto> listeFournisseurs() {
        return fournisseurRepository.findAll().stream().map(fournisseur -> {
            FournisseurDto dto = new FournisseurDto();
            BeanUtils.copyProperties(fournisseur, dto);
            return dto;
        }).toList();
    }

    @GetMapping("/api/fournisseurs/{id}")
    public FournisseurDto fournisseurById(@PathVariable Long id) {
        Fournisseure fournisseur = fournisseurRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Fournisseur introuvable : " + id));
        FournisseurDto dto = new FournisseurDto();
        BeanUtils.copyProperties(fournisseur, dto);
        return dto;
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
