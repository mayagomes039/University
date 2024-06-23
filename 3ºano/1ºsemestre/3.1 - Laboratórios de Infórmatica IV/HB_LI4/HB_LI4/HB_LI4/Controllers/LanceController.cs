using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.EntityFrameworkCore;
using HB_LI4.Data;
using HB_LI4.Models;
using Microsoft.AspNetCore.Identity;

namespace HB_LI4.Controllers
{
    public class LanceController : Controller
    {
        private readonly HB_LI4DbContext _context;
         

        public LanceController(HB_LI4DbContext context)
        {
            _context = context;
        }

        // GET: Lance
        public async Task<IActionResult> Index()
        {
            if (HttpContext.Session.GetString("Autorizado") != null)
            {
                string TipoUser = HttpContext.Session.GetString("TipoUser");

                if (TipoUser == "Cliente")
                {
                    return View(await _context.Lances.ToListAsync());
                }
            }
            return RedirectToAction("Logout", "Menu");
        }

        // GET: Lance/Details/5
        public async Task<IActionResult> Details(int? id)
        {
            if (HttpContext.Session.GetString("Autorizado") != null)
            {
                if (id == null || _context.Lances == null)
                {
                    return NotFound();
                }

                var lance = await _context.Lances
                    .FirstOrDefaultAsync(m => m.ID == id);
                if (lance == null)
                {
                    return NotFound();
                }

                return View(lance);
            }

            return RedirectToAction("Logout", "Menu");
        }

        // GET: Lance/Create
        public IActionResult Create()
        {
            string TipoUser = HttpContext.Session.GetString("TipoUser");

            if (TipoUser == "Cliente")
            {
                return View();
            }

            return RedirectToAction("Logout", "Menu");
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create([Bind("ID,Valor,FormaPagamento")] Lance lance)
        {
            string TipoUser = HttpContext.Session.GetString("TipoUser");

            if (TipoUser == "Cliente")
            {
            if (ModelState.IsValid)
            {
                string idCliente = HttpContext.Session.GetString("ClienteId");
                lance.ClienteID = idCliente;

                if (int.TryParse(HttpContext.Request.RouteValues["id"].ToString(), out int idLeilao))
                {
                    var leilao = await _context.Leiloes.FindAsync(idLeilao);

                    // Verifica se o leilão existe e se a data de fim é posterior à data atual
                    if (leilao != null && leilao.DataFim >= DateTime.Today)
                    {
                        lance.LeilaoID = idLeilao;

                        // Atualiza o valor do leilao
                        if (lance.Valor >= leilao.PrecoFinal)
                        {
                            leilao.PrecoFinal = lance.Valor;
                            await _context.SaveChangesAsync();
                        }
                    }
                    else
                    {
                        return RedirectToAction("VisualizarLeiloes", "MenuCliente");
                    }
                }
                else
                {
                    return View();
                }
                
                int ultimoId = await _context.Lances.MaxAsync(l => (int?)l.ID) ?? 0;
                lance.ID = ultimoId + 1;
                
                _context.Add(lance);
                await _context.SaveChangesAsync();
                return RedirectToAction("VisualizarLeiloes", "MenuCliente");
            }
            return View();
            }

            return RedirectToAction("Logout", "Menu");
        }

        // GET: Lance/Edit/5
        public async Task<IActionResult> Edit(int? id)
        {
            string TipoUser = HttpContext.Session.GetString("TipoUser");

            if (TipoUser == "Cliente")
            {
                if (id == null || _context.Lances == null)
                {
                    return NotFound();
                }

                var lance = await _context.Lances.FindAsync(id);
                if (lance == null)
                {
                    return NotFound();
                }

                return View(lance);
            }

            return RedirectToAction("Logout", "Menu");
        }

        // POST: Lance/Edit/5
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(int id, [Bind("ID,Valor,FormaPagamento")] Lance lance)
        {
            string TipoUser = HttpContext.Session.GetString("TipoUser");

            if (TipoUser == "Cliente")
            {
                if (id != lance.ID)
                {
                    return NotFound();
                }

                if (ModelState.IsValid)
                {
                    try
                    {
                        _context.Update(lance);
                        await _context.SaveChangesAsync();
                    }
                    catch (DbUpdateConcurrencyException)
                    {
                        if (!LanceExists(lance.ID))
                        {
                            return NotFound();
                        }
                        else
                        {
                            throw;
                        }
                    }

                    return RedirectToAction(nameof(Index));
                }

                return View(lance);
            }

            return RedirectToAction("Logout", "Menu");
        }

        // GET: Lance/Delete/5
        public async Task<IActionResult> Delete(int? id)
        {
            string TipoUser = HttpContext.Session.GetString("TipoUser");

            if (TipoUser == "Cliente")
            {
                if (id == null || _context.Lances == null)
                {
                    return NotFound();
                }

                var lance = await _context.Lances
                    .FirstOrDefaultAsync(m => m.ID == id);
                if (lance == null)
                {
                    return NotFound();
                }

                return View(lance);
            }
            return RedirectToAction("Logout", "Menu");
        }

            // POST: Lance/Delete/5
        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> DeleteConfirmed(int id)
        {
            string TipoUser = HttpContext.Session.GetString("TipoUser");

            if (TipoUser == "Cliente")
            {
            if (_context.Lances == null)
            {
                return Problem("Entity set 'HB_LI4ContextLance.Lance'  is null.");
            }
            var lance = await _context.Lances.FindAsync(id);
            if (lance != null)
            {
                _context.Lances.Remove(lance);
            }
            
            await _context.SaveChangesAsync();
            return RedirectToAction(nameof(Index));
            }
            return RedirectToAction("Logout", "Menu");
        }

        private bool LanceExists(int id)
        {
          return _context.Lances.Any(e => e.ID == id);
        }
    }
}
