const sideMenu = document.querySelector('aside');
const menuBtn = document.getElementById('menu-btn');
const closeBtn = document.getElementById('close-btn');
const darkMode = document.querySelector('.dark-mode');

// Inicializar modo oscuro desde localStorage al cargar la página
document.addEventListener('DOMContentLoaded', () => {
	// Verificar si hay una preferencia guardada
	const isDarkMode = localStorage.getItem('darkMode') === 'true';

	// Aplicar tema según la preferencia guardada
	document.body.classList.toggle('dark-mode-variables', isDarkMode);

	// Activar el icono correspondiente
	const lightModeIcon = darkMode.querySelector('span:nth-child(1)');
	const darkModeIcon = darkMode.querySelector('span:nth-child(2)');

	if (lightModeIcon && darkModeIcon) {
		lightModeIcon.classList.toggle('active', !isDarkMode);
		darkModeIcon.classList.toggle('active', isDarkMode);
	}
});

// Toggle sidebar
menuBtn.addEventListener('click', () => sideMenu.classList.add('active'));
closeBtn.addEventListener('click', () => sideMenu.classList.remove('active'));

// Toggle dark mode
darkMode.addEventListener('click', () => {
	document.body.classList.toggle('dark-mode-variables');

	const lightModeIcon = darkMode.querySelector('span:nth-child(1)');
	const darkModeIcon = darkMode.querySelector('span:nth-child(2)');

	if (lightModeIcon && darkModeIcon) {
		lightModeIcon.classList.toggle('active');
		darkModeIcon.classList.toggle('active');
	}

	// Guardar preferencia en localStorage
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