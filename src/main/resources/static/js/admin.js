document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('[data-confirm]').forEach((button) => {
        button.addEventListener('click', (event) => {
            if (!window.confirm(button.dataset.confirm)) event.preventDefault();
        });
    });

    const menuButton = document.querySelector('.admin-menu-button');
    const sidebar = document.querySelector('.admin-sidebar');
    menuButton?.addEventListener('click', () => sidebar?.classList.toggle('is-open'));

    const chart = document.querySelector('#bookingChart');
    if (chart && window.Chart) {
        const stats = window.bookingStats || {};
        new Chart(chart, {
            type: 'doughnut',
            data: {
                labels: Object.keys(stats),
                datasets: [{
                    data: Object.values(stats),
                    backgroundColor: ['#c9a76b', '#496c7b', '#8da898', '#a8afb4'],
                    borderWidth: 0,
                    hoverOffset: 5
                }]
            },
            options: {
                maintainAspectRatio: false,
                cutout: '72%',
                plugins: {
                    legend: { position: 'right', labels: { usePointStyle: true, boxWidth: 7, boxHeight: 7, padding: 18, font: { family: 'DM Sans', size: 11 } } }
                }
            }
        });
    }
});