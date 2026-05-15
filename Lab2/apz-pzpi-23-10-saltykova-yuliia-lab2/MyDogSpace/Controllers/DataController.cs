using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.IO;

namespace MyDogSpace.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize(Roles = "Admin")]
    public class DataController : ControllerBase
    {
        private readonly string _dbPath;

        public DataController(IConfiguration configuration)
        {
            // Шлях до БД з ConnectionString
            var connectionString = configuration.GetConnectionString("DefaultConnection");
            // "Data Source=../Infrastructure/DB_Storage/MyDogSpace.db;..."
            var parts = connectionString.Split(';');
            var dataSource = parts.FirstOrDefault(p => p.StartsWith("Data Source=", StringComparison.OrdinalIgnoreCase));
            _dbPath = dataSource?.Substring("Data Source=".Length) ?? "MyDogSpace.db";
            
            // Якщо шлях відносний, робимо його абсолютним відносно ContentRoot
            if (!Path.IsPathRooted(_dbPath))
            {
                _dbPath = Path.GetFullPath(Path.Combine(Directory.GetCurrentDirectory(), _dbPath));
            }
        }

        [HttpGet("export")]
        public IActionResult ExportDatabase()
        {
            if (!System.IO.File.Exists(_dbPath))
                return NotFound(new { message = "Файл бази даних не знайдено." });

            try
            {
                // Використовуємо FileStream для ефективної передачі великих файлів
                var stream = new FileStream(_dbPath, FileMode.Open, FileAccess.Read, FileShare.ReadWrite);
                return File(stream, "application/x-sqlite3", "MyDogSpace.db");
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = $"Помилка при експорті: {ex.Message}" });
            }
        }

        [HttpPost("import")]
        public async Task<IActionResult> ImportDatabase(IFormFile file)
        {
            if (file == null || file.Length == 0)
                return BadRequest(new { message = "Файл не завантажено." });

            if (!file.FileName.EndsWith(".db"))
                return BadRequest(new { message = "Невірний формат файлу. Очікується .db" });

            try
            {
                // Очищаємо пули з'єднань SQLite, щоб спробувати розблокувати файл
                // Це допоможе уникнути помилки "The process cannot access the file because it is being used by another process"
                Microsoft.Data.Sqlite.SqliteConnection.ClearAllPools();

                var tempPath = _dbPath + ".tmp";
                using (var stream = new FileStream(tempPath, FileMode.Create))
                {
                    await file.CopyToAsync(stream);
                }

                // Спробуємо замінити файл. Якщо він все ще заблокований, повернемо зрозумілу помилку.
                try 
                {
                    System.IO.File.Copy(tempPath, _dbPath, true);
                    System.IO.File.Delete(tempPath);
                }
                catch (IOException ioEx)
                {
                    System.IO.File.Delete(tempPath);
                    return BadRequest(new { message = "Не вдалося замінити файл бази даних. Можливо, він використовується іншим процесом. Спробуйте зупинити сервер перед імпортом або повторіть спробу." });
                }

                return Ok(new { message = "Базу даних успішно оновлено. Перезавантажте додаток або сервер для застосування змін." });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = $"Помилка імпорту: {ex.Message}" });
            }
        }
    }
}
