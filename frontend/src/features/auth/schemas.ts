import { z } from 'zod';

export const loginSchema = z.object({
  userName: z.string().min(2, 'Username is required'),
  password: z.string().min(6, 'At least 6 characters'),
});
export type LoginValues = z.infer<typeof loginSchema>;

export const registerSchema = z.object({
  userName: z.string().min(2, 'At least 2 characters'),
  email: z.string().email('Valid email required'),
  password: z.string().min(8, 'At least 8 characters'),
});
export type RegisterValues = z.infer<typeof registerSchema>;

