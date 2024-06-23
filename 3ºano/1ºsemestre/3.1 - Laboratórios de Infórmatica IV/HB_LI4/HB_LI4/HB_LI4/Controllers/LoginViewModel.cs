using System.ComponentModel.DataAnnotations;

namespace HB_LI4.Controllers
{
    public class LoginViewModel
    {
        [Required(ErrorMessage = "O campo Nome é obrigatório.")]
        public string Email { get; set; }

        [Required(ErrorMessage = "O campo Palavra Passe é obrigatório.")]
        [DataType(DataType.Password)]
        public string PalavraPasse { get; set; }
    }
}