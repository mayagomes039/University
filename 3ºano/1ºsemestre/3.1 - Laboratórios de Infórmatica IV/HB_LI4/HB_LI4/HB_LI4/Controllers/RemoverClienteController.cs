using Microsoft.AspNetCore.Mvc;

namespace HB_LI4.Controllers;

public class RemoverClienteController : Controller
{
    // GET: /RemoverCliente
    public  string Index()
    {
        return "Aqui pretendemos remover um cliente";
    }
    
    //GET: /RemoverCliente/removerCliente
    public  string removerCliente()
    {
        return "Aqui devolvemos os clientes todos e o admin escolhe o que pretende remover" +
               "ou : damos um formulario e ele mete o id de quem pretende remover";
    }
}