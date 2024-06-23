using Microsoft.AspNetCore.Mvc;
using System.Text.Encodings.Web;
using HB_LI4.Data;
using HB_LI4.Models;

namespace HB_LI4.Controllers;

public class GerirCategoriasController : Controller
{
    // GET: /GerirCategoria 
    //"Aqui pretendemos escolher entre criar ou editar uma categoria"
    public IActionResult Index()
    {
        if (HttpContext.Session.GetString("Autorizado") != null)
        {
            string TipoUser = HttpContext.Session.GetString("TipoUser");
            if (TipoUser == "Funcionario")
            {
                return View();
            }
        }

        return RedirectToAction("Logout", "MenuCliente");
    }


    public IActionResult CriarCategoria()
    {
        return RedirectToAction("Create", "Categoria");
    }

    private IActionResult PedirIdCategoria()
    {
        return View();
    }

    [HttpPost]
    [ValidateAntiForgeryToken]
    public IActionResult PedirIdCategoria(int idCategoria)
    {
        return RedirectToAction("Edit", "Categoria", new { id = idCategoria });
    }

    public IActionResult EditarCategoria()
    {
        if (HttpContext.Session.GetString("Autorizado") != null)
        {
            string TipoUser = HttpContext.Session.GetString("TipoUser");
            if (TipoUser == "Funcionario")
            {
                return PedirIdCategoria();
            }
        }

        return RedirectToAction("Logout", "MenuCliente");
    }


}