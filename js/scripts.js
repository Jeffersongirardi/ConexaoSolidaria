// Funções de autenticação
function handleLogin(event, type) {
  event.preventDefault();
  const email = document.getElementById("email").value;
  const senha = document.getElementById("senha").value;

  // Simulação de autenticação
  if (email && senha) {
    const redirectPage =
      type === "doador"
        ? "dashboard-doador.html"
        : "dashboard-instituicao.html";
    window.location.href = redirectPage;
    email;
  } else {
    alert("Por favor, preencha todos os campos.");
  }
}

// Funções do Dashboard
function showAddNeedForm() {
  // Implementar lógica para mostrar formulário de nova necessidade
  alert("Formulário de nova necessidade será implementado em breve.");
}

function updateProgress() {
  const progressBars = document.querySelectorAll(".progress");
  progressBars.forEach((bar) => {
    const width = bar.getAttribute("data-progress");
    bar.style.width = width + "%";
  });
}

// Funções do Blog
function filterPosts(category) {
  // Implementar lógica de filtro de posts por categoria
  console.log("Filtrando posts por categoria:", category);
}

// Inicialização
document.addEventListener("DOMContentLoaded", function () {
  // Configurar listeners de formulários
  const loginDoadorForm = document.getElementById("login-doador-form");
  if (loginDoadorForm) {
    loginDoadorForm.addEventListener("submit", (e) => handleLogin(e, "doador"));
  }

  const loginInstituicaoForm = document.getElementById(
    "login-instituicoes-form"
  );
  if (loginInstituicaoForm) {
    loginInstituicaoForm.addEventListener("submit", (e) =>
      handleLogin(e, "instituicao")
    );
  }

  // Inicializar progress bars
  updateProgress();

  // Adicionar animações de hover
  const cards = document.querySelectorAll(".stat-card, .blog-post");
  cards.forEach((card) => {
    card.addEventListener("mouseenter", function () {
      this.style.transform = "translateY(-5px)";
    });
    card.addEventListener("mouseleave", function () {
      this.style.transform = "translateY(0)";
    });
  });
});

// Função para logout
function logout() {
  // Implementar lógica de logout
  window.location.href = "../index.html";
}

// Funções de validação de formulários
function validateForm(formId) {
  const form = document.getElementById(formId);
  const inputs = form.querySelectorAll("input[required], textarea[required]");
  let isValid = true;

  inputs.forEach((input) => {
    if (!input.value.trim()) {
      isValid = false;
      input.classList.add("error");
    } else {
      input.classList.remove("error");
    }
  });

  return isValid;
}
