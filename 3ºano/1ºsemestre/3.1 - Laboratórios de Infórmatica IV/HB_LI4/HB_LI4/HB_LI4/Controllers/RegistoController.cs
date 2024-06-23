using Microsoft.AspNetCore.Mvc;

namespace HB_LI4.Controllers;

public class RegistoController : Controller
{
    // GET: /Registo
    public  IActionResult Index()
    {
        return View();
        // "Aqui devolvemos um formulario para inserir os dados para o registo;" +
               //" verificamos os dados e atualizamos a bd";
    }
    
}