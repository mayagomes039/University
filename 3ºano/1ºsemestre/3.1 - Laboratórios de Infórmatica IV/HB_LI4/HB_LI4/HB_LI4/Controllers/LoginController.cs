using HB_LI4.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Http;
using BCrypt.Net;
using HB_LI4.Data;
using Microsoft.EntityFrameworkCore;

namespace HB_LI4.Controllers
{
    public class LoginController : Controller
    {
        // GET
        public IActionResult Index()
        {
            if (HttpContext.Session.GetString("Erro") != null)
                ViewBag.Erro = HttpContext.Session.GetString("Erro");
            return View();
        }

        [HttpPost]
        public IActionResult verLogIn()
        {
            var email = Request.Form["Email"];
            var senha = Request.Form["PalavraPasse"];

            var cliente = LoginCliente(email, senha);

            if (cliente != null)
            {
                // O usuário é um cliente
                HttpContext.Session.SetString("Autorizado", "OK");
                HttpContext.Session.SetString("ClienteId", cliente.Id);
                HttpContext.Session.SetString("TipoUser", "Cliente");
                HttpContext.Session.Remove("Erro");
                return RedirectToAction("Index", "MenuCliente");
            }
            else
            {
                var funcionario = LoginFuncionario(email, senha);
                if (funcionario != null)
                {
                    // O usuário é um funcionário
                    HttpContext.Session.SetString("Autorizado", "OK");
                    HttpContext.Session.SetString("FuncionarioId", funcionario.Id);
                    HttpContext.Session.SetString("TipoUser", "Funcionario");
                    HttpContext.Session.Remove("Erro");
                    return RedirectToAction("Index", "Menu");
                }
            }

            HttpContext.Session.SetString("Erro", "Senha ou cliente/funcionário inválido");
            return RedirectToAction("Index", "Login");
        }

        private Cliente LoginCliente(string email, string senha)
        {
            try
            {
                var optionsBuilder = new DbContextOptionsBuilder<HB_LI4DbContext>();
                optionsBuilder.UseSqlite("Data Source=HB_LI4.Data.db");

                using (var context = new HB_LI4DbContext(optionsBuilder.Options))
                {
                    var cliente = context.Clientes.SingleOrDefault(c => c.Email == email);

                    if (cliente != null && senha == cliente.PalavraPasse)
                    {
                        return cliente;
                    }
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine("An error occurred: " + ex.Message);
            }

            return null;
        }

        private Funcionario LoginFuncionario(string email, string senha)
        {
            try
            {
                var optionsBuilder = new DbContextOptionsBuilder<HB_LI4DbContext>();
                optionsBuilder.UseSqlite("Data Source=HB_LI4.Data.db");

                using (var context = new HB_LI4DbContext(optionsBuilder.Options))
                {
                    var funcionario = context.Funcionarios.SingleOrDefault(f => f.Email == email);

                    if (funcionario != null && senha == funcionario.PalavraPasse)
                    {
                        return funcionario;
                    }
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine("An error occurred: " + ex.Message);
            }

            return null;
        }
    }
}