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
    public class ClienteController : Controller
    {
        private HB_LI4DbContext _context;

        public ClienteController(HB_LI4DbContext context)
        {
            _context = context;
        }

        // GET: Cliente
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
        
        

        // GET: Cliente/Details/id
        public async Task<IActionResult> Details(string? id)
        {
            if (HttpContext.Session.GetString("Autorizado") != null)
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

            return RedirectToAction("Logout", "MenuCliente");
        }

        // GET: Cliente/Create
        public IActionResult Create()
        {
            return View();
        }

        // POST: Cliente/Create
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create([Bind("ClienteId,Name,Email,PalavraPasse,Telemovel")] Cliente cliente)
        {
            if (ModelState.IsValid)
            {
                _context.Add(cliente);
                await _context.SaveChangesAsync();
                return RedirectToAction(nameof(HomeController.Index)); //redireciona para login
            }
            return View(cliente);
        }

        // GET: Cliente/Edit/5
        public async Task<IActionResult> Edit(string id)
        {
            if (HttpContext.Session.GetString("Autorizado") != null)
            {
                if (id == null || _context.Clientes == null)
                {
                    return NotFound();
                }

                var cliente = await _context.Clientes.FindAsync(id);

                return View(cliente);
            }

            return RedirectToAction("Logout", "MenuCliente");
            
        }

        // POST: Cliente/Edit/5
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(string id, [Bind("Id,Name,Email,PalavraPasse,Telemovel")] Cliente cliente)
        {
            if (HttpContext.Session.GetString("Autorizado") != null)
            {
                if (id != cliente.Id)
                {
                    return NotFound();
                }

                if (ModelState.IsValid)
                {
                    try
                    {
                        _context.Update(cliente);
                        await _context.SaveChangesAsync();
                    }
                    catch (DbUpdateConcurrencyException)
                    {
                        if (!ClienteExists(cliente.Id))
                        {
                            return NotFound();
                        }
                    }

                    return RedirectToAction("Details", "Cliente", new { id = cliente.Id });
                }

                return View(cliente);
            }
            return RedirectToAction("Logout", "MenuCliente");
        }

            // GET: Cliente/Delete/5
            public async Task<IActionResult> Delete(string? id)
            {
                string TipoUser = HttpContext.Session.GetString("TipoUser");

                if (TipoUser == "Funcionario")
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
                return RedirectToAction("Logout", "MenuCliente");
            }

            // POST: Cliente/Delete/5
        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> DeleteConfirmed(int id)
        {
            string TipoUser = HttpContext.Session.GetString("TipoUser");

            if (TipoUser == "Funcionario")
            {
                if (_context.Clientes == null)
                {
                    return Problem("Entity set 'HB_LI4Context.Cliente'  is null.");
                }

                var cliente = await _context.Clientes.FindAsync(id);
                if (cliente != null)
                {
                    _context.Clientes.Remove(cliente);
                }

                await _context.SaveChangesAsync();
                return RedirectToAction(nameof(Index));
            }

            return RedirectToAction("Logout", "MenuCliente");
        }


        private bool ClienteExists(string id)
        {
          return (_context.Clientes?.Any(e => e.Id == id)).GetValueOrDefault();
        }
    }
    
}
