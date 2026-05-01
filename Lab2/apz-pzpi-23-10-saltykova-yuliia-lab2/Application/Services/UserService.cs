using Application.Abstractions.Interfaces;
using Application.DTOs;
using Entities.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace Application.Services
{
    public class UserService : IUserService
    {
        private readonly IUserRepository _userRepository;

        public UserService(IUserRepository userRepository)
        {
            _userRepository = userRepository;
        }

        // Профільні операції для користувача
        public async Task<UserProfileDto> GetMyProfileAsync(int userId)
        {
            var user = await _userRepository.GetUserByIdAsync(userId);
            if (user == null)
            {
                throw new Exception("Користувача не знайдено.");
            }

            // Оновлюємо останню активність
            await _userRepository.UpdateLastActivityAsync(userId, DateTime.UtcNow);

            return MapToUserProfileDto(user);
        }

        public async Task<UserProfileDto> UpdateMyProfileAsync(int userId, UpdateUserProfileDto updateDto)
        {
            var user = await _userRepository.GetUserByIdAsync(userId);
            if (user == null)
            {
                throw new Exception("Користувача не знайдено.");
            }

            if (user.IsBlocked)
            {
                throw new Exception("Ваш профіль заблоковано. Зверніться до адміністратора.");
            }

            // Оновлюємо дані
            if (!string.IsNullOrWhiteSpace(updateDto.Username))
            {
                user.Username = updateDto.Username;
            }

            if (updateDto.Bio != null)
            {
                user.Bio = updateDto.Bio;
            }

            if (updateDto.PhotoUrl != null)
            {
                user.PhotoUrl = updateDto.PhotoUrl;
            }

            if (updateDto.LastLatitude.HasValue)
            {
                user.LastLatitude = updateDto.LastLatitude;
            }

            if (updateDto.LastLongitude.HasValue)
            {
                user.LastLongitude = updateDto.LastLongitude;
            }

            user.LastActivity = DateTime.UtcNow;
            var updatedUser = await _userRepository.UpdateUserAsync(user);

            return MapToUserProfileDto(updatedUser);
        }

        public async Task<bool> DeleteMyProfileAsync(int userId)
        {
            var user = await _userRepository.GetUserByIdAsync(userId);
            if (user == null)
            {
                throw new Exception("Користувача не знайдено.");
            }

            return await _userRepository.DeleteUserAsync(userId);
        }

        public async Task<IEnumerable<UserProfileDto>> SearchUsersAsync(string? query, double? lat, double? lon, double? radius, int currentUserId)
        {
            var users = await _userRepository.GetAllUsersAsync();
            var friendIds = (await _userRepository.GetFriendsAsync(currentUserId)).Select(f => f.Id).ToHashSet();
            
            // Виключаємо поточного користувача зі списку
            var filtered = users.Where(u => u.Id != currentUserId && !friendIds.Contains(u.Id)).AsEnumerable();

            if (!string.IsNullOrWhiteSpace(query))
            {
                var lowerQuery = query.ToLower();
                filtered = filtered.Where(u => u.Username.ToLower().Contains(lowerQuery) || (u.Bio != null && u.Bio.ToLower().Contains(lowerQuery)));
            }

            return filtered.Select(u => MapToUserProfileDto(u, friendIds.Contains(u.Id)));
        }

        private double CalculateDistance(double lat1, double lon1, double lat2, double lon2)
        {
            var R = 6371; // Radius of the earth in km
            var dLat = ToRadians(lat2 - lat1);
            var dLon = ToRadians(lon2 - lon1);
            var a = Math.Sin(dLat / 2) * Math.Sin(dLat / 2) +
                    Math.Cos(ToRadians(lat1)) * Math.Cos(ToRadians(lat2)) *
                    Math.Sin(dLon / 2) * Math.Sin(dLon / 2);
            var c = 2 * Math.Atan2(Math.Sqrt(a), Math.Sqrt(1 - a));
            return R * c;
        }

        private double ToRadians(double deg) => deg * (Math.PI / 180);

        // Адміністративні операції
        public async Task<UserStatisticsDto> GetUserStatisticsAsync()
        {
            var users = await _userRepository.GetAllUsersAsync();
            var now = DateTime.UtcNow;
            var thirtyDaysAgo = now.AddDays(-30);

            return new UserStatisticsDto
            {
                TotalUsers = users.Count,
                ActiveUsers = users.Count(u => u.LastActivity.HasValue && u.LastActivity.Value >= thirtyDaysAgo),
                BlockedUsers = users.Count(u => u.IsBlocked),
                TotalRegularUsers = users.Count(u => u.Role == UserRole.DogOwner),
                TotalAdmins = users.Count(u => u.Role == UserRole.Admin)
            };
        }

        public async Task<List<UserActivityDto>> GetAllUsersActivityAsync()
        {
            var users = await _userRepository.GetAllUsersAsync();
            return users.Select(u => MapToUserActivityDto(u)).ToList();
        }

        public async Task<UserActivityDto> GetUserActivityAsync(int userId)
        {
            var user = await _userRepository.GetUserByIdAsync(userId);
            if (user == null)
            {
                throw new Exception("Користувача не знайдено.");
            }

            return MapToUserActivityDto(user);
        }

        public async Task<bool> BlockUserAsync(int userId, string blockReason)
        {
            var user = await _userRepository.GetUserByIdAsync(userId);
            if (user == null)
            {
                throw new Exception("Користувача не знайдено.");
            }

            if (user.Role == UserRole.Admin)
            {
                throw new Exception("Не можна заблокувати адміністратора.");
            }

            user.IsBlocked = true;
            user.BlockReason = blockReason;
            await _userRepository.UpdateUserAsync(user);

            return true;
        }

        public async Task<bool> UnblockUserAsync(int userId)
        {
            var user = await _userRepository.GetUserByIdAsync(userId);
            if (user == null)
            {
                throw new Exception("Користувача не знайдено.");
            }

            user.IsBlocked = false;
            user.BlockReason = null;
            await _userRepository.UpdateUserAsync(user);

            return true;
        }

        // Friendship
        public async Task<bool> AddFriendAsync(int userId, int friendId)
        {
            if (userId == friendId) throw new Exception("Ви не можете додати себе у друзі.");
            return await _userRepository.AddFriendAsync(userId, friendId);
        }

        public async Task<bool> RemoveFriendAsync(int userId, int friendId)
        {
            return await _userRepository.RemoveFriendAsync(userId, friendId);
        }

        public async Task<IEnumerable<UserProfileDto>> GetFriendsAsync(int userId)
        {
            var friends = await _userRepository.GetFriendsAsync(userId);
            return friends.Select(f => MapToUserProfileDto(f, true));
        }

        // Маппінг
        private UserProfileDto MapToUserProfileDto(User user, bool isFriend = false)
        {
            return new UserProfileDto
            {
                Id = user.Id,
                Username = user.Username,
                Email = user.Email,
                Bio = user.Bio,
                PhotoUrl = user.PhotoUrl,
                Role = user.Role,
                LastLatitude = user.LastLatitude,
                LastLongitude = user.LastLongitude,
                LastActivity = user.LastActivity,
                IsBlocked = user.IsBlocked,
                BlockReason = user.BlockReason,
                IsFriend = isFriend
            };
        }

        private UserActivityDto MapToUserActivityDto(User user)
        {
            return new UserActivityDto
            {
                Id = user.Id,
                Username = user.Username,
                Email = user.Email,
                LastActivity = user.LastActivity,
                IsBlocked = user.IsBlocked,
                BlockReason = user.BlockReason
            };
        }
    }
}
