using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.EntityFrameworkCore;
using HB_LI4.Data;
using HB_LI4.Models;

namespace HB_LI4.Controllers
{
    public class LeilaoController : Controller
    {
        private readonly HB_LI4DbContext _context;

        public LeilaoController(HB_LI4DbContext context)
        {
            _context = context;
        }


        public async Task<IActionResult> Index(string Categoria)
        {
            string TipoUser = HttpContext.Session.GetString("TipoUser");

            if (TipoUser == "Funcionario")
            {
                IQueryable<string> genreQuery = from m in _context.Categorias
                    orderby m.ID
                    select m.Nome;

                var leiloes = from m in _context.Leiloes
                    select m;

                if (!string.IsNullOrEmpty(Categoria))
                {
                    leiloes = leiloes.Where(x => x.Categoria.Nome == Categoria);
                }

                var leiloesCategorias = new LeilaoCategoriaViewModel
                {
                    Categorias = new SelectList(await genreQuery.Distinct().ToListAsync()),
                    Leiloes = await leiloes.ToListAsync()
                };

                return View(leiloesCategorias);
            }
            return RedirectToAction("Logout", "MenuCliente");
        }


        // GET: Leilao
        /*public async Task<IActionResult> Index()
        {
              return View(await _context.Leilao.ToListAsync());
        }
        */

        // GET: Leilao/Details/5
        public async Task<IActionResult> Details(int? id)
        {
            string TipoUser = HttpContext.Session.GetString("TipoUser");

            if (TipoUser == "Funcionario")
            {
            if (id == null || _context.Leiloes == null)
            {
                return NotFound();
            }

            var leilao = await _context.Leiloes
                .FirstOrDefaultAsync(m => m.ID == id);
            if (leilao == null)
            {
                return NotFound();
            }

            return View(leilao);
            }
            return RedirectToAction("Logout", "MenuCliente");
        }

        // GET: Leilao/Create
        public IActionResult Create()
        {
            string TipoUser = HttpContext.Session.GetString("TipoUser");

            if (TipoUser == "Funcionario")
            {
            ViewBag.Categorias = new SelectList(_context.Categorias, "ID", "Nome");
            return View();
            }
            return RedirectToAction("Logout", "MenuCliente");
        }


        // POST: Leilao/Create
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create(
            [Bind("ID,PrecoInicial,PrecoFinal,Nome,DataInicio,DataFim,CategoriaID, Imagem")] Leilao leilao,
            IFormFile imagem)
        {
            string TipoUser = HttpContext.Session.GetString("TipoUser");

            if (TipoUser == "Funcionario")
            {
                if (ModelState.IsValid)
                {
                    // Definir ClienteID como nulo
                    leilao.ClienteID = null;

                    // Obter FuncionarioId da sessão e atribuir a FuncionarioID
                    string funcionarioId = HttpContext.Session.GetString("FuncionarioId");
                    leilao.FuncionarioID = funcionarioId;

                    int categoriaId;
                    if (Int32.TryParse(Request.Form["CategoriaID"], out categoriaId))
                    {
                        leilao.CategoriaID = categoriaId;
                        leilao.Categoria = await _context.Categorias.FindAsync(categoriaId);
                    }

                    // Processar a imagem enviada
                    if (imagem != null && imagem.Length > 0)
                    {
                        using (var memoryStream = new MemoryStream())
                        {
                            await imagem.CopyToAsync(memoryStream);
                            leilao.Imagem = memoryStream.ToArray();
                        }
                    }

                    _context.Add(leilao);
                    await _context.SaveChangesAsync();
                    return RedirectToAction("Index", "Menu");
                }

                return View(leilao);
            }
            return RedirectToAction("Logout", "MenuCliente");
        }


        // GET: Leilao/Edit/5
        public async Task<IActionResult> Edit(int? id)
        {
            string TipoUser = HttpContext.Session.GetString("TipoUser");

            if (TipoUser == "Funcionario")
            {
                if (id == null || _context.Leiloes == null)
                {
                    return NotFound();
                }

                var leilao = await _context.Leiloes.FindAsync(id);
                if (leilao == null)
                {
                    return NotFound();
                }
                ViewBag.Categorias = new SelectList(_context.Categorias, "ID", "Nome");

                return View(leilao);
            }

            return RedirectToAction("Logout", "MenuCliente");
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(int id, [Bind("ID,PrecoInicial,PrecoFinal,Nome,DataInicio,DataFim,Categoria,Imagem")] Leilao leilao,IFormFile imagem)
        {
            string TipoUser = HttpContext.Session.GetString("TipoUser");

            if (TipoUser == "Funcionario")
            {
                if (id != leilao.ID)
                {
                    return NotFound();
                }

               
                var leilaoExistente = await _context.Leiloes
                    .AsNoTracking() 
                    .FirstOrDefaultAsync(l => l.ID == id);

                if (leilaoExistente == null)
                {
                    return NotFound();
                }

                // Mantém o FuncionarioID existente no leilão sendo editado
                leilao.FuncionarioID = leilaoExistente.FuncionarioID;
                leilao.ClienteID = leilaoExistente.ClienteID;

                if (ModelState.IsValid)
                {
                    if (imagem == null)
                    {
                        // Se o campo de imagem estiver vazio, mantém a imagem existente
                        leilao.Imagem = leilaoExistente.Imagem;
                    }
                    if (imagem != null && imagem.Length > 0)
                    {
                        using (var memoryStream = new MemoryStream())
                        {
                            await imagem.CopyToAsync(memoryStream);
                            leilao.Imagem = memoryStream.ToArray();
                        }
                    }
                    try
                    {
                        _context.Update(leilao);
                        await _context.SaveChangesAsync();
                    }
                    catch (DbUpdateConcurrencyException)
                    {
                        if (!LeilaoExists(leilao.ID))
                        {
                            return NotFound();
                        }
                        else
                        {
                            throw;
                        }
                    }
                    return RedirectToAction("VisualizarLeiloes", "Menu");
                }
                return View(leilao);
            }

            return RedirectToAction("Logout", "MenuCliente");
        }


        public async Task<IActionResult> Delete(int? id)
        {
            string TipoUser = HttpContext.Session.GetString("TipoUser");

            if (TipoUser == "Funcionario")
            {
                if (id == null || _context.Leiloes == null)
                {
                    return NotFound();
                }

                var leilao = await _context.Leiloes
                    .Include(l => l.Lances) // Inclua a coleção de lances
                    .FirstOrDefaultAsync(m => m.ID == id);

                if (leilao == null)
                {
                    return NotFound();
                }

                return View(leilao);
            }

            return RedirectToAction("Logout", "MenuCliente");
        }

        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> DeleteConfirmed(int id)
        {
            string TipoUser = HttpContext.Session.GetString("TipoUser");

            if (TipoUser == "Funcionario")
            {
                if (_context.Leiloes == null)
                {
                    return Problem("Entity set 'HB_LI4ContextLeilao.Leilao' is null.");
                }

                var leilao = await _context.Leiloes
                    .Include(l => l.Lances) // Inclua a coleção de lances
                    .FirstOrDefaultAsync(m => m.ID == id);

                if (leilao != null)
                {
                    // Remova todos os lances associados ao leilão
                    _context.Lances.RemoveRange(leilao.Lances);

                    // Em seguida, remova o próprio leilão
                    _context.Leiloes.Remove(leilao);

                    await _context.SaveChangesAsync();
                    return RedirectToAction("VisualizarLeiloes", "Menu");
                }
            }

            return RedirectToAction("Logout", "MenuCliente");
        }


        private bool LeilaoExists(int id)
        {
          return _context.Leiloes.Any(e => e.ID == id);
        }
    }
}
