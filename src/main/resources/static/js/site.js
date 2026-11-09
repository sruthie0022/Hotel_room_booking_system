document.addEventListener('DOMContentLoaded', () => {
    const revealItems = document.querySelectorAll('.reveal');
    revealItems.forEach((item, index) => {
        item.style.setProperty('--reveal-delay', `${index * 100}ms`);
        item.classList.add('is-visible');
    });

    const checkIn = document.querySelector('input[name="checkIn"]');
    const checkOut = document.querySelector('input[name="checkOut"]');
    if (checkIn && checkOut) {
        checkIn.addEventListener('change', () => {
            if (!checkIn.value) return;
            const nextDate = new Date(`${checkIn.value}T00:00:00`);
            nextDate.setDate(nextDate.getDate() + 1);
            const minimum = nextDate.toISOString().slice(0, 10);
            checkOut.min = minimum;
            if (checkOut.value && checkOut.value < minimum) checkOut.value = minimum;
        });
    }

    const summary = document.querySelector('#booking-summary');
    if (summary) {
        const arrival = document.querySelector('input[name="checkIn"]');
        const departure = document.querySelector('input[name="checkOut"]');
        const price = Number(summary.dataset.price);
        const nightsLabel = document.querySelector('#night-count');
        const totalLabel = document.querySelector('#booking-total');
        const updateTotal = () => {
            if (!arrival.value || !departure.value) return;
            const nights = Math.max(0, Math.round((new Date(`${departure.value}T00:00:00`) - new Date(`${arrival.value}T00:00:00`)) / 86400000));
            nightsLabel.textContent = nights;
            totalLabel.textContent = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(nights * price);
        };
        arrival.addEventListener('change', updateTotal);
        departure.addEventListener('change', updateTotal);
        updateTotal();
    }

    const sorter = document.querySelector('#sort-rooms');
    sorter?.addEventListener('change', () => {
        const params = new URLSearchParams(window.location.search);
        params.set('sort', sorter.value);
        window.location.assign(`${sorter.dataset.baseUrl}?${params.toString()}`);
    });

    document.querySelectorAll('[data-confirm]').forEach((button) => {
        button.addEventListener('click', (event) => {
            if (!window.confirm(button.dataset.confirm)) event.preventDefault();
        });
    });

    document.querySelectorAll('form[data-loading]').forEach((form) => {
        form.addEventListener('submit', () => {
            const button = form.querySelector('button[type="submit"]');
            if (!button) return;
            const indicator = document.createElement('span');
            indicator.className = 'loading-indicator';
            indicator.setAttribute('aria-hidden', 'true');
            button.disabled = true;
            button.setAttribute('aria-busy', 'true');
            button.replaceChildren(indicator, document.createTextNode(form.dataset.loadingLabel || 'Please wait...'));
        });
    });
});