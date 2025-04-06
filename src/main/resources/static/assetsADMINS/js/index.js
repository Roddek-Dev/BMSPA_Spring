const sideMenu = document.querySelector('aside');
const menuBtn = document.getElementById('menu-btn');
const closeBtn = document.getElementById('close-btn');
const darkMode = document.querySelector('.dark-mode');

// Inicializar modo oscuro
const initDarkMode = () => {
	const isDarkMode = localStorage.getItem('darkMode') === 'true';
	document.body.classList.toggle('dark-mode-variables', isDarkMode);
	darkMode.querySelector('span:nth-child(1)').classList.toggle('active', !isDarkMode);
	darkMode.querySelector('span:nth-child(2)').classList.toggle('active', isDarkMode);
};

// Toggle sidebar
menuBtn.addEventListener('click', () => sideMenu.classList.add('active'));
closeBtn.addEventListener('click', () => sideMenu.classList.remove('active'));

// Toggle dark mode
darkMode.addEventListener('click', () => {
	document.body.classList.toggle('dark-mode-variables');
	darkMode.querySelector('span:nth-child(1)').classList.toggle('active');
	darkMode.querySelector('span:nth-child(2)').classList.toggle('active');
	localStorage.setItem('darkMode', document.body.classList.contains('dark-mode-variables'));
});
// Función para marcar el enlace activo en el sidebar
function setActiveLink(linkId) {
	const link = document.getElementById(linkId);
	if (link) {
		// Remover active de todos los enlaces primero
		document.querySelectorAll('aside .sidebar a').forEach(item => {
			item.classList.remove('active');
		});
		// Agregar active al enlace actual
		link.classList.add('active');
	}
}
// Confirmación de eliminación mejorada
document.querySelectorAll('.btn-delete').forEach(btn => {
	btn.addEventListener('click', function(e) {
		if (!confirm('¿Estás seguro de eliminar este campo?')) {
			e.preventDefault();
		}
	});
});

// Iniciar
document.addEventListener('DOMContentLoaded', initDarkMode);