(() => {
    'use strict';

    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content || '';
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || '';

    const state = {
        produits: [],
        categories: [],
        fournisseurs: [],
        tags: [],
        mode: 'post',
        editingId: null,
        saleProduct: null
    };

    const $ = (id) => document.getElementById(id);

    const form = $('medicamentForm');
    const corpsTable = $('produitsBody');
    const categorieSelect = $('categorie');
    const fournisseurSelect = $('fournisseur');
    const produitSelect = $('produitSelect');
    const tagsBox = $('tagsBox');
    const submitBtn = $('submitBtn');
    const submitLabel = $('submitLabel');
    const endpointPreview = $('endpointPreview');
    const modeButtons = document.querySelectorAll('.mode-btn');

    const moneyFormatter = new Intl.NumberFormat('fr-FR', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });

    const dateFormatter = new Intl.DateTimeFormat('fr-FR', {
        day: '2-digit',
        month: 'short',
        year: 'numeric'
    });

    const ICONS = {
        sell: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="9" cy="20" r="1.6"/><circle cx="18" cy="20" r="1.6"/><path d="M2 3h3l2.6 12.2a2 2 0 0 0 2 1.6h7.8a2 2 0 0 0 2-1.6L21 7H6"/></svg>',
        stock: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M20 7 12 3 4 7v10l8 4 8-4Z"/><path d="m4 7 8 4 8-4"/><path d="M12 21V11"/></svg>',
        edit: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M12 20h9"/><path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4Z"/></svg>',
        remove: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M3 6h18"/><path d="M8 6V4h8v2"/><path d="m19 6-1 14H6L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/></svg>'
    };

    /* ---------- Utilitaires ---------- */

    function escapeHtml(value) {
        return String(value ?? '').replace(/[&<>"']/g, (char) => ({
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#39;'
        })[char]);
    }

    function startOfDay(date) {
        const copy = new Date(date);
        copy.setHours(0, 0, 0, 0);
        return copy;
    }

    function parseDate(value) {
        if (!value) return null;
        const date = new Date(value + 'T00:00:00');
        return Number.isNaN(date.getTime()) ? null : date;
    }

    function formatPrice(value) {
        if (value === null || value === undefined || value === '') return '—';
        const number = Number(value);
        return Number.isFinite(number) ? moneyFormatter.format(number) + ' DH' : '—';
    }

    function expiryInfo(value) {
        const date = parseDate(value);
        if (!date) {
            return { label: '—', className: 'gray', title: "Aucune date d'expiration" };
        }

        const today = startOfDay(new Date());
        const days = Math.round((date - today) / 86400000);
        const label = dateFormatter.format(date);

        if (days < 0) {
            return { label, className: 'red', title: 'Expiré depuis ' + Math.abs(days) + ' jour(s)' };
        }
        if (days <= 30) {
            return { label, className: 'amber', title: 'Expire dans ' + days + ' jour(s)' };
        }
        return { label, className: 'green', title: 'Expire dans ' + days + ' jour(s)' };
    }

    function stockInfo(value) {
        const stock = Number(value ?? 0);
        if (stock <= 0) {
            return { stock, label: 'Rupture', className: 'red' };
        }
        if (stock < 5) {
            return { stock, label: stock + ' · faible', className: 'amber' };
        }
        return { stock, label: stock + ' unités', className: 'green' };
    }

    /* ---------- Appels API ---------- */

    function apiFetch(url, options = {}) {
        const method = (options.method || 'GET').toUpperCase();
        const headers = new Headers(options.headers || {});
        headers.set('Accept', 'application/json');

        if (method !== 'GET' && method !== 'HEAD' && csrfToken && csrfHeader) {
            headers.set(csrfHeader, csrfToken);
        }

        return fetch(url, { ...options, headers });
    }

    async function errorMessage(response) {
        const fallback = 'Erreur HTTP ' + response.status +
            (response.statusText ? ' (' + response.statusText + ')' : '');

        try {
            const text = await response.text();
            if (!text) return fallback;

            try {
                const data = JSON.parse(text);
                return data.message || data.detail || data.error || fallback;
            } catch {
                return text.length < 220 ? text : fallback;
            }
        } catch {
            return fallback;
        }
    }

    /* ---------- Notification ---------- */

    let toastTimer = null;

    function showToast(message, type = 'info') {
        const toast = $('toast');
        $('toastMessage').textContent = message;
        toast.className = 'toast show ' + type;

        window.clearTimeout(toastTimer);
        toastTimer = window.setTimeout(() => toast.classList.remove('show'), 4500);
    }

    function hideToast() {
        $('toast').classList.remove('show');
        window.clearTimeout(toastTimer);
    }

    /* ---------- Affichage ---------- */

    function renderStats() {
        const today = startOfDay(new Date());
        const produits = state.produits;

        $('statProduits').textContent = produits.length;
        $('statDisponibles').textContent =
            produits.filter((produit) => Number(produit.stock ?? 0) > 0).length;
        $('statStockFaible').textContent = produits.filter((produit) => {
            const stock = Number(produit.stock ?? 0);
            return stock > 0 && stock < 5;
        }).length;
        $('statExpires').textContent = produits.filter((produit) => {
            const date = parseDate(produit.dateExpiration);
            return date !== null && date < today;
        }).length;
    }

    function renderTagChecks() {
        if (!tagsBox) return;

        const checkedIds = new Set(
            Array.from(tagsBox.querySelectorAll('input[type="checkbox"]:checked'))
                .map((input) => Number(input.value))
        );

        if (!state.tags.length) {
            tagsBox.innerHTML = '<span class="muted" style="font-size:.8rem;">Aucun tag disponible.</span>';
            return;
        }

        tagsBox.innerHTML = state.tags.map((tag) => {
            const checked = checkedIds.has(Number(tag.id)) ? ' checked' : '';
            return '<label class="tag-check">' +
                '<input type="checkbox" value="' + tag.id + '"' + checked + '>' +
                '<span>' + escapeHtml(tag.libelle) + '</span>' +
                '</label>';
        }).join('');
    }

    function renderSelectOptions() {
        if (!APP.canEdit) return;

        const categorieValue = categorieSelect.value;
        categorieSelect.innerHTML =
            '<option value="">— Choisir —</option>' +
            state.categories.map((categorie) =>
                '<option value="' + categorie.id + '">' + escapeHtml(categorie.nom) + '</option>'
            ).join('');
        if (categorieValue) categorieSelect.value = categorieValue;

        const fournisseurValue = fournisseurSelect.value;
        fournisseurSelect.innerHTML =
            '<option value="">— Choisir —</option>' +
            state.fournisseurs.map((fournisseur) =>
                '<option value="' + fournisseur.id + '">' + escapeHtml(fournisseur.nom) + '</option>'
            ).join('');
        if (fournisseurValue) fournisseurSelect.value = fournisseurValue;

        renderTagChecks();
        renderProductOptions();
        updateFormChrome();
    }

    function renderProductOptions() {
        if (!produitSelect) return;

        produitSelect.innerHTML =
            '<option value="">— Sélectionner un médicament —</option>' +
            state.produits.map((produit) =>
                '<option value="' + produit.id + '">#' + produit.id + ' · ' +
                escapeHtml(produit.nom) + '</option>'
            ).join('');

        if (state.editingId && state.produits.some((produit) => produit.id === state.editingId)) {
            produitSelect.value = String(state.editingId);
        } else {
            produitSelect.value = '';
            if (state.mode === 'put') state.editingId = null;
        }
    }

    function actionsHtml(produit) {
        const stock = stockInfo(produit.stock);
        const nom = escapeHtml(produit.nom);
        const actions = [];

        if (APP.canSell) {
            actions.push(
                '<button type="button" class="icon-btn" data-action="sell" data-id="' + produit.id + '"' +
                (stock.stock <= 0 ? ' disabled' : '') +
                ' title="Vendre" aria-label="Vendre ' + nom + '">' + ICONS.sell + '</button>'
            );
        }
        if (APP.canStock) {
            actions.push(
                '<button type="button" class="icon-btn" data-action="stock" data-id="' + produit.id + '"' +
                ' title="Modifier le stock" aria-label="Modifier le stock de ' + nom + '">' + ICONS.stock + '</button>'
            );
        }
        if (APP.canEdit) {
            actions.push(
                '<button type="button" class="icon-btn" data-action="edit" data-id="' + produit.id + '"' +
                ' title="Modifier" aria-label="Modifier ' + nom + '">' + ICONS.edit + '</button>'
            );
        }
        if (APP.canDelete) {
            actions.push(
                '<button type="button" class="icon-btn danger" data-action="delete" data-id="' + produit.id + '"' +
                ' title="Supprimer" aria-label="Supprimer ' + nom + '">' + ICONS.remove + '</button>'
            );
        }

        return actions.length ? actions.join('') : '<span class="muted">—</span>';
    }

    function renderTable() {
        const query = $('searchInput').value.trim().toLowerCase();

        const produits = state.produits.filter((produit) => {
            if (APP.isClient && Number(produit.stock ?? 0) <= 0) return false;
            if (!query) return true;

            const tags = Array.isArray(produit.tags)
                ? produit.tags.map((tag) => tag.libelle).join(' ')
                : '';

            const haystack = [
                produit.nom,
                produit.description,
                produit.codeBarre,
                produit.categorie?.nom,
                produit.fournisseur?.nom,
                produit.detailProduit?.fabricant,
                tags
            ].join(' ').toLowerCase();

            return haystack.includes(query);
        });

        $('resultCount').textContent =
            produits.length === 1 ? '1 médicament' : produits.length + ' médicaments';

        if (!produits.length) {
            let message = 'Aucun résultat pour « ' + escapeHtml(query) + ' »';
            let hint = 'Essayez un autre nom, une catégorie ou un tag.';

            if (!state.produits.length) {
                message = APP.isClient
                    ? 'Aucun médicament disponible'
                    : 'Aucun médicament enregistré';
                hint = APP.isClient
                    ? 'Le catalogue est momentanément vide.'
                    : 'Utilisez le formulaire ci-dessus pour ajouter votre premier médicament.';
            }

            corpsTable.innerHTML = '<tr><td colspan="8" class="empty">' +
                '<strong>' + message + '</strong>' + hint + '</td></tr>';
            return;
        }

        corpsTable.innerHTML = produits.map((produit) => {
            const categorie = produit.categorie?.nom || '—';
            const fournisseur = produit.fournisseur?.nom || '—';
            const contact = produit.fournisseur?.contact || '';
            const fabricant = produit.detailProduit?.fabricant || '';
            const expiry = expiryInfo(produit.dateExpiration);
            const stock = stockInfo(produit.stock);

            const tags = Array.isArray(produit.tags) ? produit.tags : [];
            const tagsHtml = tags.slice(0, 3).map((tag) =>
                '<span class="chip">' + escapeHtml(tag.libelle) + '</span>'
            ).join('') || '<span class="muted">—</span>';

            const moreTags = tags.length > 3
                ? '<span class="chip">+' + (tags.length - 3) + '</span>'
                : '';

            const meta = [
                produit.codeBarre || 'Sans code-barres',
                fabricant
            ].filter(Boolean).map(escapeHtml).join(' · ');

            return '<tr>' +
                '<td>' +
                    '<div class="cell-title">' + escapeHtml(produit.nom) + '</div>' +
                    '<div class="cell-meta">' + meta + '</div>' +
                '</td>' +
                '<td><span class="badge gray">' + escapeHtml(categorie) + '</span></td>' +
                '<td class="price">' + formatPrice(produit.prix) + '</td>' +
                '<td><span class="badge ' + stock.className + '" title="Stock : ' + stock.stock + '">' +
                    stock.label + '</span></td>' +
                '<td><span class="badge ' + expiry.className + '" title="' + escapeHtml(expiry.title) + '">' +
                    expiry.label + '</span></td>' +
                '<td>' +
                    '<div>' + escapeHtml(fournisseur) + '</div>' +
                    (contact ? '<div class="cell-meta">' + escapeHtml(contact) + '</div>' : '') +
                '</td>' +
                '<td><div class="chips">' + tagsHtml + moreTags + '</div></td>' +
                '<td><div class="actions">' + actionsHtml(produit) + '</div></td>' +
            '</tr>';
        }).join('');
    }

    /* ---------- Formulaire ---------- */

    function updateFormChrome() {
        if (!form) return;

        const isPut = state.mode === 'put';
        $('formTitle').textContent = isPut ? 'Modifier un médicament' : 'Ajouter un médicament';
        $('formSubtitle').textContent = isPut
            ? 'Sélectionnez un médicament, ajustez les champs puis enregistrez.'
            : 'Créer un médicament et définir son stock initial.';

        submitLabel.textContent = isPut ? 'Enregistrer les modifications' : 'Ajouter le médicament';
        endpointPreview.textContent = isPut
            ? 'PUT /api/produits/' + (state.editingId ?? '{id}')
            : 'POST /api/produits';
        $('putSelector').classList.toggle('hidden', !isPut);
    }

    function resetFields() {
        if (!form) return;

        form.reset();
        state.editingId = null;
        if (produitSelect) produitSelect.value = '';
        if (tagsBox) {
            tagsBox.querySelectorAll('input[type="checkbox"]').forEach((input) => {
                input.checked = false;
            });
        }
    }

    function setMode(mode, options = {}) {
        state.mode = mode;

        modeButtons.forEach((button) => {
            const active = button.dataset.mode === mode;
            button.classList.toggle('active', active);
            button.classList.toggle('primary', active);
            button.setAttribute('aria-pressed', String(active));
        });

        if (!options.preserve) resetFields();
        updateFormChrome();
    }

    function fillForm(produit) {
        if (!form) return;

        $('nom').value = produit.nom ?? '';
        $('description').value = produit.description ?? '';
        $('prix').value = produit.prix ?? '';
        $('stock').value = produit.stock ?? 0;
        $('codeBarre').value = produit.codeBarre ?? '';
        $('dateExpiration').value = produit.dateExpiration ?? '';

        categorieSelect.value = produit.categorie?.id ?? '';
        fournisseurSelect.value = produit.fournisseur?.id ?? '';

        const detail = produit.detailProduit ?? {};
        $('fabricant').value = detail.fabricant ?? '';
        $('paysOrigine').value = detail.paysOrigine ?? '';
        $('composition').value = detail.composition ?? '';
        $('dosage').value = detail.dosage ?? '';
        $('indications').value = detail.indications ?? '';

        const tagIds = new Set(
            (Array.isArray(produit.tags) ? produit.tags : []).map((tag) => Number(tag.id))
        );
        tagsBox.querySelectorAll('input[type="checkbox"]').forEach((input) => {
            input.checked = tagIds.has(Number(input.value));
        });
    }

    function buildPayload() {
        const tags = Array.from(tagsBox.querySelectorAll('input[type="checkbox"]:checked'))
            .map((input) => ({ id: Number(input.value) }));

        return {
            nom: $('nom').value.trim(),
            description: $('description').value.trim(),
            prix: $('prix').value === '' ? null : Number($('prix').value),
            stock: $('stock').value === '' ? 0 : Number($('stock').value),
            codeBarre: $('codeBarre').value.trim(),
            dateExpiration: $('dateExpiration').value || null,
            categorie: { id: Number(categorieSelect.value) },
            fournisseur: { id: Number(fournisseurSelect.value) },
            detailProduit: {
                fabricant: $('fabricant').value.trim(),
                paysOrigine: $('paysOrigine').value.trim(),
                composition: $('composition').value.trim(),
                dosage: $('dosage').value.trim(),
                indications: $('indications').value.trim()
            },
            tags: tags
        };
    }

    function startEdit(id) {
        const produit = state.produits.find((item) => item.id === id);
        if (!produit) return;

        setMode('put', { preserve: true });
        state.editingId = produit.id;
        produitSelect.value = String(produit.id);
        fillForm(produit);
        updateFormChrome();

        $('formCard').scrollIntoView({ behavior: 'smooth', block: 'start' });
        window.setTimeout(() => $('nom').focus({ preventScroll: true }), 320);
    }

    /* ---------- Actions ---------- */

    async function deleteProduct(id) {
        const produit = state.produits.find((item) => item.id === id);
        if (!produit) return;
        if (!window.confirm('Supprimer « ' + produit.nom + ' » ? Cette action est irréversible.')) {
            return;
        }

        try {
            const response = await apiFetch(API.produits + '/' + id, { method: 'DELETE' });
            if (!response.ok) throw new Error(await errorMessage(response));

            showToast('« ' + produit.nom + ' » a été supprimé.', 'success');
            if (state.editingId === id) setMode('post');
            await loadAll();
        } catch (error) {
            showToast(error.message || 'Erreur lors de la suppression.', 'error');
        }
    }

    async function adjustStock(id) {
        const produit = state.produits.find((item) => item.id === id);
        if (!produit) return;

        const current = Number(produit.stock ?? 0);
        const value = window.prompt(
            'Nouveau stock pour « ' + produit.nom + ' » (actuel : ' + current + ') :',
            String(current)
        );
        if (value === null) return;

        const stock = Number(value.trim());
        if (!Number.isInteger(stock) || stock < 0) {
            showToast('Le stock doit être un entier positif ou nul.', 'error');
            return;
        }

        try {
            const response = await apiFetch(API.produits + '/' + id + '/stock', {
                method: 'PATCH',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ stock })
            });
            if (!response.ok) throw new Error(await errorMessage(response));

            showToast('Stock de « ' + produit.nom + ' » mis à jour : ' + stock + '.', 'success');
            await loadAll();
        } catch (error) {
            showToast(error.message || 'Erreur lors de la mise à jour du stock.', 'error');
        }
    }

    /* ---------- Vente ---------- */

    function updateSaleTotal() {
        const quantite = Number($('saleQty').value);
        const prix = Number(state.saleProduct?.prix ?? 0);

        $('saleTotal').textContent =
            Number.isFinite(quantite) && quantite > 0
                ? moneyFormatter.format(quantite * prix) + ' DH'
                : '—';
    }

    function openSale(id) {
        const produit = state.produits.find((item) => item.id === id);
        if (!produit) return;

        const stock = Number(produit.stock ?? 0);
        if (stock <= 0) {
            showToast('Ce médicament est en rupture de stock.', 'error');
            return;
        }

        state.saleProduct = produit;
        $('saleProductName').textContent = produit.nom;
        $('saleStock').textContent = stock + ' unité(s)';
        $('salePrice').textContent = formatPrice(produit.prix);
        $('saleQty').max = String(stock);
        $('saleQty').value = '1';
        updateSaleTotal();
        $('saleModal').classList.remove('hidden');
        $('saleQty').focus();
        $('saleQty').select();
    }

    function closeSale() {
        $('saleModal').classList.add('hidden');
        state.saleProduct = null;
    }

    async function confirmSale() {
        const produit = state.saleProduct;
        if (!produit) return;

        const quantite = Number($('saleQty').value);
        const stock = Number(produit.stock ?? 0);

        if (!Number.isInteger(quantite) || quantite <= 0) {
            showToast('La quantité doit être un entier supérieur à zéro.', 'error');
            return;
        }
        if (quantite > stock) {
            showToast('Stock insuffisant : ' + stock + ' unité(s) disponible(s).', 'error');
            return;
        }

        const button = $('saleConfirm');
        button.disabled = true;

        try {
            const response = await apiFetch(API.ventes, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ produitId: produit.id, quantite })
            });
            if (!response.ok) throw new Error(await errorMessage(response));

            const vente = await response.json();
            closeSale();
            showToast(
                'Vente enregistrée : ' + produit.nom + ' × ' + quantite +
                ' · ' + formatPrice(vente.montant),
                'success'
            );
            await loadAll();
        } catch (error) {
            showToast(error.message || "Erreur lors de l'enregistrement de la vente.", 'error');
        } finally {
            button.disabled = false;
        }
    }

    /* ---------- Chargement ---------- */

    async function loadAll() {
        const refresh = $('refreshBtn');
        refresh.disabled = true;

        try {
            const [produits, categories, fournisseurs, tags] = await Promise.all([
                apiFetch(API.produits).then(checkResponse),
                apiFetch(API.categories).then(checkResponse),
                apiFetch(API.fournisseurs).then(checkResponse),
                apiFetch(API.tags).then(checkResponse)
            ]);

            state.produits = produits;
            state.categories = categories;
            state.fournisseurs = fournisseurs;
            state.tags = tags;

            renderStats();
            renderSelectOptions();
            renderTable();

            $('lastUpdate').textContent = new Date().toLocaleTimeString('fr-FR', {
                hour: '2-digit',
                minute: '2-digit'
            });
        } catch (error) {
            showToast(error.message || 'Impossible de charger les données.', 'error');
            corpsTable.innerHTML = '<tr><td colspan="8" class="empty">' +
                '<strong>Impossible de charger les données</strong>' +
                escapeHtml(error.message || "Vérifiez que l'API est démarrée.") +
                '</td></tr>';
        } finally {
            refresh.disabled = false;
        }
    }

    async function checkResponse(response) {
        if (!response.ok) throw new Error(await errorMessage(response));
        return response.json();
    }

    /* ---------- Événements ---------- */

    modeButtons.forEach((button) => {
        button.addEventListener('click', () => {
            if (button.dataset.mode === state.mode) return;
            setMode(button.dataset.mode);
        });
    });

    if (produitSelect) {
        produitSelect.addEventListener('change', () => {
            const produit = state.produits.find(
                (item) => String(item.id) === produitSelect.value
            );

            if (produit) {
                state.editingId = produit.id;
                fillForm(produit);
            } else {
                resetFields();
            }
            updateFormChrome();
        });
    }

    if (form) {
        form.addEventListener('submit', async (event) => {
            event.preventDefault();

            if (state.mode === 'put' && !state.editingId) {
                showToast("Choisissez d'abord le médicament à modifier.", 'error');
                produitSelect.focus();
                return;
            }
            if (!form.reportValidity()) return;

            const isPut = state.mode === 'put';
            const url = isPut ? API.produits + '/' + state.editingId : API.produits;

            submitBtn.disabled = true;
            submitLabel.textContent = 'Enregistrement…';

            try {
                const response = await apiFetch(url, {
                    method: isPut ? 'PUT' : 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(buildPayload())
                });
                if (!response.ok) throw new Error(await errorMessage(response));

                const saved = await response.json();
                showToast(
                    isPut
                        ? '« ' + saved.nom + ' » a été modifié.'
                        : '« ' + saved.nom + ' » a été ajouté.',
                    'success'
                );

                setMode('post');
                await loadAll();
            } catch (error) {
                showToast(error.message || "Erreur lors de l'enregistrement.", 'error');
            } finally {
                submitBtn.disabled = false;
                updateFormChrome();
            }
        });

        $('resetBtn').addEventListener('click', () => {
            setMode(state.mode);
            $('nom').focus();
        });
    }

    $('refreshBtn').addEventListener('click', () => loadAll());
    $('searchInput').addEventListener('input', renderTable);

    corpsTable.addEventListener('click', (event) => {
        const button = event.target.closest('button[data-action]');
        if (!button) return;

        const id = Number(button.dataset.id);
        if (button.dataset.action === 'sell') openSale(id);
        if (button.dataset.action === 'stock') adjustStock(id);
        if (button.dataset.action === 'edit') startEdit(id);
        if (button.dataset.action === 'delete') deleteProduct(id);
    });

    $('toastClose').addEventListener('click', hideToast);

    $('saleQty').addEventListener('input', updateSaleTotal);
    $('saleClose').addEventListener('click', closeSale);
    $('saleCancel').addEventListener('click', closeSale);
    $('saleConfirm').addEventListener('click', confirmSale);
    $('saleModal').addEventListener('click', (event) => {
        if (event.target === $('saleModal')) closeSale();
    });
    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape' && !$('saleModal').classList.contains('hidden')) {
            closeSale();
        }
    });

    /* ---------- Démarrage ---------- */

    if (form) {
        setMode('post');
    }
    loadAll();
})();
