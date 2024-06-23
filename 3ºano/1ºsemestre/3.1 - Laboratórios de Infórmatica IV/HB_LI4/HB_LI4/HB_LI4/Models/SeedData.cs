using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using HB_LI4.Data;
using System;
using System.Linq;

namespace HB_LI4.Models
{
    public static class SeedData
    {
        public static void Initialize(IServiceProvider serviceProvider)
        {
            using (var context = new HB_LI4DbContext(
                       serviceProvider.GetRequiredService<
                           DbContextOptions<HB_LI4DbContext>>()))
            {
                // Look for any cliente.
                if (context.Clientes.Any())
                {
                    return;   // DB has been seeded
                }

                context.Clientes.AddRange(
                    new Cliente()
                    {
                        Id = "1",
                        Email = "maria@gmail.com",
                        Name = "maria",
                        PalavraPasse = "11",
                        Telemovel = 123456789
                    },

                    new Cliente()
                    {
                        Id = "2",
                        Email = "joao@gmail.com",
                        Name = "Joao",
                        PalavraPasse = "21",
                        Telemovel = 345267189
                    },

                    new Cliente()
                    {
                        Id = "3",
                        Email = "pedro@gmail.com",
                        Name = "Pedro",
                        PalavraPasse = "2132",
                        Telemovel = 768098241
                    },

                    new Cliente()
                    {
                        Id = "4",
                        Email = "jaona@gmail.com",
                        Name = "Joana",
                        PalavraPasse = "123",
                        Telemovel = 145623789
                    }
                );
                context.SaveChanges();
            }
        }
    }
}