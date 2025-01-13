const statisticTypeSelect = document.getElementById('statisticType');
let chart;

function renderChart(statisticData) {
    const ctx = document.getElementById('achievementChart').getContext('2d');

    if (chart) {
        chart.destroy();
    }

    chart = new Chart(ctx, {
        type: document.getElementById('chartType').value,
        data: {
            labels: statisticData.map(item => item.name),
            datasets: [{
                label: "Amount users",
                data: statisticData.map(item => item.count),
                backgroundColor: generateRandomColors(statisticData.length)
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: 'top' }
            }
        }
    });
}

document.addEventListener('DOMContentLoaded', function () {
    updateData()
});

document.getElementById('chartType').addEventListener('change', function (e) {
    updateData()
});

document.getElementById('statisticType').addEventListener('change', function (e) {
    updateData()
});

document.getElementById('chartSize').addEventListener('change', function (e) {
    const chartContainer = document.querySelector('.chart-container');
    const scaleFactor = parseFloat(e.target.value);
    chartContainer.style.height = `${65 * scaleFactor}vh`;
});

async function updateData() {
    const url = statisticTypeSelect.value;
    if (url === "default"){
        renderChart(statisticalData);
    }else {
        renderChart(await fetchStatisticsData(url))
    }
}

async function fetchStatisticsData(url) {
    try {
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return await response.json();
    } catch (error) {
        console.error('Fetch error:', error);
        return [];
    }
}

function generateRandomColors(count) {
    const colors = [];
    for (let i = 0; i < count; i++) {
        colors.push(`#${Math.floor(Math.random()*16777215).toString(16)}`);
    }
    return colors;
}