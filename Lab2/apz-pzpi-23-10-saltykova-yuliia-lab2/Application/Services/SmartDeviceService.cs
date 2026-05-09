using Application.Abstractions.Interfaces;
using Application.DTOs;
using Entities.Models;
using Microsoft.Extensions.Configuration;
using Microsoft.IdentityModel.Tokens;
using System.Security.Claims;

namespace Application.Services
{
    public class SmartDeviceService : ISmartDeviceService
    {
        private readonly ISmartDeviceRepository _deviceRepository;
        private readonly IDogRepository _dogRepository;
        private readonly IConfiguration _configuration;

        public SmartDeviceService(ISmartDeviceRepository deviceRepository, IDogRepository dogRepository, IConfiguration configuration)
        {
            _deviceRepository = deviceRepository;
            _dogRepository = dogRepository;
            _configuration = configuration;
        }

        public async Task<IEnumerable<SmartDeviceDto>> GetAllDevicesAsync(int userId, string userRole)
        {
            IEnumerable<SmartDevice> devices;

            // Якщо користувач - адміністратор, показуємо всі пристрої
            if (userRole == "Admin")
            {
                devices = await _deviceRepository.GetAllAsync();
            }
            else
            {
                // Інакше показуємо тільки пристрої собак цього користувача
                devices = await _deviceRepository.GetByUserIdAsync(userId);
            }

            return devices.Select(d => MapToDto(d));
        }

        public async Task<SmartDeviceDto?> GetDeviceByIdAsync(int id, int userId)
        {
            var device = await _deviceRepository.GetByIdAsync(id);
            if (device == null) return null;

            if (device.DogId.HasValue)
            {
                var dog = await _dogRepository.GetByIdAsync(device.DogId.Value);
                if (dog != null && dog.OwnerId != userId)
                    throw new UnauthorizedAccessException("Ви не маєте доступу до цього пристрою");
            }

            return MapToDto(device);
        }

        public async Task<SmartDeviceDto?> GetDeviceByDogIdAsync(int dogId, int userId)
        {
            var dog = await _dogRepository.GetByIdAsync(dogId);
            if (dog == null)
                throw new Exception("Собака не знайдена");

            if (dog.OwnerId != userId)
                throw new UnauthorizedAccessException("Ви не маєте доступу до цієї собаки");

            var device = await _deviceRepository.GetByDogIdAsync(dogId);
            if (device == null) return null;

            return MapToDto(device);
        }

        public async Task<SmartDeviceDto> CreateDeviceAsync(CreateSmartDeviceDto deviceDto, int userId)
        {
            var dog = await _dogRepository.GetByIdAsync(deviceDto.DogId);
            if (dog == null)
                throw new Exception("Собака не знайдена");

            if (dog.OwnerId != userId)
                throw new UnauthorizedAccessException("Ви не маєте доступу до цієї собаки");

            var existingDevice = await _deviceRepository.GetByDogIdAsync(deviceDto.DogId);
            if (existingDevice != null)
                throw new Exception("До цієї собаки вже прикріплений пристрій");

            var device = new SmartDevice
            {
                DeviceGuid = deviceDto.DeviceGuid,
                DogId = deviceDto.DogId,
                LastLatitude = 0,
                LastLongitude = 0,
                BatteryLevel = 100
            };

            var createdDevice = await _deviceRepository.AddAsync(device);
            return MapToDto(createdDevice, dog.Name);
        }

        public async Task UpdateDeviceAsync(int id, UpdateSmartDeviceDto deviceDto, int userId)
        {
            var device = await _deviceRepository.GetByIdAsync(id);
            if (device == null)
                throw new Exception("Пристрій не знайдено");

            if (device.DogId.HasValue)
            {
                var dog = await _dogRepository.GetByIdAsync(device.DogId.Value);
                if (dog != null && dog.OwnerId != userId)
                    throw new UnauthorizedAccessException("Ви не маєте доступу до цього пристрою");
            }

            // Розрахунок дистанції, якщо координати вже були встановлені
            if (device.LastLatitude != 0 && device.LastLongitude != 0)
            {
                double distance = CalculateDistance(
                    device.LastLatitude, device.LastLongitude,
                    deviceDto.LastLatitude, deviceDto.LastLongitude
                );
                device.TotalDistance += distance;
            }

            device.LastLatitude = deviceDto.LastLatitude;
            device.LastLongitude = deviceDto.LastLongitude;
            device.BatteryLevel = deviceDto.BatteryLevel;

            await _deviceRepository.UpdateAsync(device);
        }

        public async Task DeleteDeviceAsync(int id, int userId)
        {
            var device = await _deviceRepository.GetByIdAsync(id);
            if (device == null)
                throw new Exception("Пристрій не знайдено");

            if (device.DogId.HasValue)
            {
                var dog = await _dogRepository.GetByIdAsync(device.DogId.Value);
                if (dog != null && dog.OwnerId != userId)
                    throw new UnauthorizedAccessException("Ви не маєте доступу до цього пристрою");
            }

            await _deviceRepository.DeleteAsync(id);
        }

        // Методи для роботи пристрою
        public async Task<SmartDeviceAuthDto> RegisterDeviceAsync(string deviceGuid)
        {
            var normalizedGuid = deviceGuid.Trim().ToLower();
            Console.WriteLine($"[IOT] Registering/Logging in device: {normalizedGuid}");

            var device = await _deviceRepository.GetByDeviceGuidAsync(normalizedGuid);
            
            if (device == null)
            {
                // Якщо пристрій новий, створюємо його без прив'язки до собаки
                device = new SmartDevice
                {
                    DeviceGuid = deviceGuid.Trim(),
                    DogId = null,
                    LastLatitude = 0,
                    LastLongitude = 0,
                    BatteryLevel = 100
                };
                device = await _deviceRepository.AddAsync(device);
                Console.WriteLine($"[IOT] Created NEW device entry for: {normalizedGuid}");
            }

            var token = CreateToken(device);

            return new SmartDeviceAuthDto
            {
                Device = MapToDto(device),
                Token = token
            };
        }

        private string CreateToken(SmartDevice device)
        {
            var claims = new List<Claim>
            {
                new Claim(ClaimTypes.NameIdentifier, device.Id.ToString()),
                new Claim(ClaimTypes.SerialNumber, device.DeviceGuid),
                new Claim(ClaimTypes.Role, "Device")
            };

            var key = new SymmetricSecurityKey(System.Text.Encoding.UTF8.GetBytes(_configuration.GetSection("AppSettings:Token").Value));
            var creds = new SigningCredentials(key, SecurityAlgorithms.HmacSha512Signature);

            var tokenDescriptor = new SecurityTokenDescriptor
            {
                Subject = new ClaimsIdentity(claims),
                Expires = DateTime.Now.AddYears(1), // Токен для пристрою діє довше
                SigningCredentials = creds
            };

            var tokenHandler = new System.IdentityModel.Tokens.Jwt.JwtSecurityTokenHandler();
            var token = tokenHandler.CreateToken(tokenDescriptor);

            return tokenHandler.WriteToken(token);
        }

        public async Task<int?> GetDogIdByDeviceGuidAsync(string deviceGuid)
        {
            var normalizedGuid = deviceGuid.Trim().ToLower();
            var device = await _deviceRepository.GetByDeviceGuidAsync(normalizedGuid);
            
            if (device == null)
            {
                Console.WriteLine($"[IOT] Device NOT FOUND for GUID: {normalizedGuid}");
                return null;
            }

            // Якщо собака ще не призначена, повертаємо null
            if (device.DogId == null || device.DogId == 0)
            {
                Console.WriteLine($"[IOT] Device {normalizedGuid} found (ID: {device.Id}), but NO DOG assigned.");
                return null;
            }

            Console.WriteLine($"[IOT] Device {normalizedGuid} is assigned to Dog ID: {device.DogId}");
            return device.DogId;
        }

        public async Task AssignDeviceToDogAsync(string deviceGuid, int dogId, int userId)
        {
            var normalizedGuid = deviceGuid.Trim().ToLower();
            Console.WriteLine($"[API] Attempting to assign GUID '{normalizedGuid}' to Dog {dogId} by User {userId}");

            // 1. Знаходимо пристрій
            var device = await _deviceRepository.GetByDeviceGuidAsync(normalizedGuid);
            if (device == null)
            {
                Console.WriteLine($"[API] GUID '{normalizedGuid}' not found. Creating a NEW device entry.");
                device = new SmartDevice
                {
                    DeviceGuid = deviceGuid.Trim(),
                    LastLatitude = 0,
                    LastLongitude = 0,
                    BatteryLevel = 100
                };
                device = await _deviceRepository.AddAsync(device);
            }

            if (dogId <= 0)
                throw new Exception($"Некоректний ID собаки: {dogId}");

            // 2. Отримуємо список собак користувача, щоб перевірити права власності
            var userDogs = await _dogRepository.GetByOwnerIdAsync(userId);
            var dogList = userDogs.ToList();

            // Шукаємо конкретну собаку за ID серед собак користувача
            var targetDog = dogList.FirstOrDefault(d => d.Id == dogId);

            if (targetDog == null)
            {
                var availableDogIds = string.Join(", ", dogList.Select(d => d.Id));
                throw new Exception($"Собаку з ID {dogId} не знайдено серед ваших собак (Ваш ID: {userId}, доступні ID собак: {availableDogIds})");
            }

            // 3. Перевіряємо, чи вже є пристрій у цієї собаки
            var existingDevice = await _deviceRepository.GetByDogIdAsync(targetDog.Id);

            // Якщо у собаки є пристрій, і це НЕ той самий, який ми намагаємося підключити
            if (existingDevice != null && existingDevice.Id != device.Id)
                throw new Exception($"До собаки '{targetDog.Name}' вже прикріплений інший пристрій ({existingDevice.DeviceGuid})");

            // 4. Прив'язуємо пристрій до вибраної собаки
            device.DogId = targetDog.Id;
            await _deviceRepository.UpdateAsync(device);
        }

        public async Task UnassignDeviceFromDogAsync(int dogId, int userId)
        {
            Console.WriteLine($"[API] Attempting to UNASSIGN device from Dog {dogId} by User {userId}");

            // 1. Перевіряємо права власності на собаку
            var dog = await _dogRepository.GetByIdAsync(dogId);
            if (dog == null || dog.OwnerId != userId)
                throw new Exception("Собака не знайдена або не належить вам.");

            // 2. Знаходимо пристрій, прив'язаний до цієї собаки
            var device = await _deviceRepository.GetByDogIdAsync(dogId);
            if (device != null)
            {
                device.DogId = null;
                await _deviceRepository.UpdateAsync(device);
                Console.WriteLine($"[API] Device {device.DeviceGuid} successfully unassigned from Dog {dogId}");
            }
            else
            {
                Console.WriteLine($"[API] No device found for Dog {dogId}. Nothing to unassign.");
            }
        }

        public async Task UpdateDeviceTelemetryAsync(int id, UpdateSmartDeviceDto deviceDto)
        {
            // Відправка телеметрії БЕЗ перевірки прав доступу (для IoT пристроїв)
            var device = await _deviceRepository.GetByIdAsync(id);
            if (device == null)
                throw new Exception("Пристрій не знайдено");

            // Розрахунок дистанції, якщо координати вже були встановлені
            if (device.LastLatitude != 0 && device.LastLongitude != 0)
            {
                double distance = CalculateDistance(
                    device.LastLatitude, device.LastLongitude,
                    deviceDto.LastLatitude, deviceDto.LastLongitude
                );
                device.TotalDistance += distance;
            }

            device.LastLatitude = deviceDto.LastLatitude;
            device.LastLongitude = deviceDto.LastLongitude;
            device.BatteryLevel = deviceDto.BatteryLevel;

            await _deviceRepository.UpdateAsync(device);
        }

        /// <summary>
        /// Розрахунок відстані між двома точками за формулою Haversine
        /// </summary>
        /// <returns>Дистанція в метрах</returns>
        private static double CalculateDistance(double lat1, double lon1, double lat2, double lon2)
        {
            const double R = 6371000; // Радіус Землі в метрах

            double lat1Rad = DegreesToRadians(lat1);
            double lat2Rad = DegreesToRadians(lat2);
            double deltaLat = DegreesToRadians(lat2 - lat1);
            double deltaLon = DegreesToRadians(lon2 - lon1);

            double a = Math.Sin(deltaLat / 2) * Math.Sin(deltaLat / 2) +
                      Math.Cos(lat1Rad) * Math.Cos(lat2Rad) *
                      Math.Sin(deltaLon / 2) * Math.Sin(deltaLon / 2);

            double c = 2 * Math.Atan2(Math.Sqrt(a), Math.Sqrt(1 - a));

            return R * c; // Повертає відстань в метрах
        }

        private static double DegreesToRadians(double degrees)
        {
            return degrees * Math.PI / 180.0;
        }

        private static SmartDeviceDto MapToDto(SmartDevice d, string? dogName = null)
        {
            return new SmartDeviceDto
            {
                Id = d.Id,
                DeviceGuid = d.DeviceGuid,
                LastLatitude = d.LastLatitude,
                LastLongitude = d.LastLongitude,
                BatteryLevel = d.BatteryLevel,
                TotalDistance = d.TotalDistance,
                DogId = d.DogId,
                DogName = dogName ?? d.Dog?.Name
            };
        }
    }
}
