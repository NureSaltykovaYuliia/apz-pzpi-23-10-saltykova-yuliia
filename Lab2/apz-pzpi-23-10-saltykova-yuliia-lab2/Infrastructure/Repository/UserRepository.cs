using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Application.Abstractions.Interfaces;
using Entities.Models;

using Microsoft.EntityFrameworkCore;

namespace Infrastructure.Repositories
{
    public class UserRepository : IUserRepository
    {
        private readonly MyDbContext _context;

        public UserRepository(MyDbContext context)
        {
            _context = context;
        }

        public async Task<User> GetUserByEmailAsync(string email)
        {
            return await _context.Users.FirstOrDefaultAsync(u => u.Email == email);
        }

        public async Task<User> GetUserByIdAsync(int id)
        {
            return await _context.Users.FirstOrDefaultAsync(u => u.Id == id);
        }

        public async Task<bool> DoesUserExistAsync(string email)
        {
            return await _context.Users.AnyAsync(u => u.Email == email);
        }

        public async Task<User> AddUserAsync(User user)
        {
            _context.Users.Add(user);
            await _context.SaveChangesAsync();
            return user;
        }

        public async Task<User> UpdateUserAsync(User user)
        {
            _context.Users.Update(user);
            await _context.SaveChangesAsync();
            return user;
        }

        public async Task<bool> DeleteUserAsync(int id)
        {
            var user = await GetUserByIdAsync(id);
            if (user == null)
            {
                return false;
            }

            _context.Users.Remove(user);
            await _context.SaveChangesAsync();
            return true;
        }

        public async Task<List<User>> GetAllUsersAsync()
        {
            return await _context.Users.ToListAsync();
        }

        public async Task UpdateLastActivityAsync(int userId, DateTime lastActivity)
        {
            var user = await GetUserByIdAsync(userId);
            if (user != null)
            {
                user.LastActivity = lastActivity;
                await _context.SaveChangesAsync();
            }
        }

        public async Task<bool> AddFriendAsync(int userId, int friendId)
        {
            var user = await _context.Users.Include(u => u.Friends).FirstOrDefaultAsync(u => u.Id == userId);
            var friend = await _context.Users.FirstOrDefaultAsync(u => u.Id == friendId);

            if (user == null || friend == null) return false;
            if (user.Friends.Any(f => f.Id == friendId)) return true;

            user.Friends.Add(friend);
            await _context.SaveChangesAsync();
            return true;
        }

        public async Task<bool> RemoveFriendAsync(int userId, int friendId)
        {
            var user = await _context.Users.Include(u => u.Friends).FirstOrDefaultAsync(u => u.Id == userId);
            if (user == null) return false;

            var friend = user.Friends.FirstOrDefault(f => f.Id == friendId);
            if (friend == null) return false;

            user.Friends.Remove(friend);
            await _context.SaveChangesAsync();
            return true;
        }

        public async Task<List<User>> GetFriendsAsync(int userId)
        {
            var user = await _context.Users.Include(u => u.Friends).FirstOrDefaultAsync(u => u.Id == userId);
            return user?.Friends.ToList() ?? new List<User>();
        }

        public async Task<bool> IsFriendAsync(int userId, int friendId)
        {
            return await _context.Users
                .Where(u => u.Id == userId)
                .AnyAsync(u => u.Friends.Any(f => f.Id == friendId));
        }
    }
}