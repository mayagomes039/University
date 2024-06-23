using HB_LI4.Data;
using HB_LI4.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.EntityFrameworkCore;

namespace HB_LI4.Controllers;

public class MenuController : Controller

{

    private readonly HB_LI4DbContext _context;
    

    public MenuController(HB_LI4DbContext context)
    {
        _context = context;
       
    }
    
    // get : /Menu
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
        return Logout();
    }
    
    public async Task<IActionResult> VisualizarLeiloes(string Categoria, decimal? PrecoMaximo)
    {
        string TipoUser = HttpContext.Session.GetString("TipoUser");
        if (TipoUser == "Funcionario")
        {
            // Obter o ID da categoria correspondente ao nome
            int? categoriaID = null;
            if (!string.IsNullOrEmpty(Categoria))
            {
                Categoria categoriaSelecionada = await _context.Categorias
                    .FirstOrDefaultAsync(c => c.Nome == Categoria);

                if (categoriaSelecionada != null)
                {
                    categoriaID = categoriaSelecionada.ID;
                }
            }

            IQueryable<string> genreQuery = from m in _context.Categorias
                orderby m.ID
                select m.Nome;

            // Modificação para incluir a propriedade de navegação Categoria
            var leiloes = from m in _context.Leiloes.Include(l => l.Categoria)
                where m.DataFim >= DateTime.Today // Filtrar pela data de fim maior ou igual à data de hoje
                select m;

            if (categoriaID.HasValue)
            {
                leiloes = leiloes.Where(x => x.CategoriaID == categoriaID.Value);
            }

            if (PrecoMaximo.HasValue)
            {
                leiloes = leiloes.Where(x => x.PrecoFinal <= PrecoMaximo.Value);
            }
            
            foreach (var leilao in leiloes)
            {
                leilao.Imagem = await _context.Leiloes
                    .Where(l => l.ID == leilao.ID)
                    .Select(l => l.Imagem)
                    .FirstOrDefaultAsync();
            }

            var leiloesCategorias = new LeilaoCategoriaViewModel
            {
                Categorias = new SelectList(await genreQuery.Distinct().ToListAsync()),
                Leiloes = await leiloes.ToListAsync(),
                PrecoMaximo = PrecoMaximo
            };

            return View(leiloesCategorias);
        }
        return RedirectToAction("Index", "Login");
    }

    
    public async Task<IActionResult> ConsultarPerfil( [FromServices] HB_LI4DbContext context)
    {
        if (HttpContext.Session.GetString("Autorizado") != null)
        {
            string id = HttpContext.Session.GetString("FuncionarioId");
            var funcionario = await context.Funcionarios.FirstOrDefaultAsync(c => c.Id == id);

            if (funcionario == null)
            {
                return NotFound();
            }

            return RedirectToAction("Details", "Funcionario", new { id = funcionario.Id });
        }
        else
        {
            return RedirectToAction("Index", "Login");
        }
    }

    
    public IActionResult CriarLeilao()
    {
        if (HttpContext.Session.GetString("Autorizado") != null)
        {
            return RedirectToAction("Create", "Leilao");
        }
        else
        {
            return RedirectToAction("Index", "Login");
        }
    }

    public async Task<IActionResult> HistoricoVendas([FromServices] HB_LI4DbContext context)
    {
        if (HttpContext.Session.GetString("Autorizado") != null)
        {
        string id = HttpContext.Session.GetString("FuncionarioId");
        var leiloesHistoricoVendas = await context.Leiloes
            .Where(l => l.DataFim < DateTime.Today && l.FuncionarioID == id)
            .ToListAsync();

        return View(leiloesHistoricoVendas);
        }
        else
        {
            return RedirectToAction("Index", "Login");
        }
    }
    
    public async Task<IActionResult> VisualizarClientes([FromServices] HB_LI4DbContext context)
    {
        if (HttpContext.Session.GetString("Autorizado") != null)
        {
            var clientes = await context.Clientes.ToListAsync();
            return View(clientes);
        }
        else
        {
            return RedirectToAction("Index", "Login");
        }
    }
    
    public async Task<IActionResult> Delete(string? id)
    {
        if (id == null || _context.Clientes == null)
        {
            return NotFound();
        }

        var cliente = await _context.Clientes
            .FirstOrDefaultAsync(m => m.Id == id);
        if (cliente == null)
        {
            return NotFound();
        }

        return View(cliente);
    }

    
    [HttpPost, ActionName("Delete")]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> DeleteConfirmed(string id)
    {
        Console.WriteLine("ola");
        if (_context.Clientes == null)
        {
            return Problem("Entity set 'HB_LI4ContextClientes.Clientes'  is null.");
        }
        var cliente = await _context.Clientes.FindAsync(id);
        if (cliente != null)
        {
            _context.Clientes.Remove(cliente);
        }
            
        await _context.SaveChangesAsync();
        return RedirectToAction(nameof(Index));
    }
    
    public IActionResult GerirCategorias([FromServices] HB_LI4DbContext context)
    {
        if (HttpContext.Session.GetString("Autorizado") != null)
        {
        return View();
        }
        else
        {
            return RedirectToAction("Index", "Login");
        }
    }
    
    public IActionResult Logout()
    {
        if (HttpContext.Session.GetString("Autorizado") != null)
        {
        HttpContext.Session.Clear(); 
        return RedirectToAction("Index", "Login");
        }
        else
        {
            return RedirectToAction("Index", "Login");
        }
    }
   

}
    
