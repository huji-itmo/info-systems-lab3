import { z } from 'zod';

export const spaceMarineSchema = z.object({
  name: z.string().min(1, 'Name is required'),
  coordinatesId: z.number().int().positive('Valid Coordinates ID required'),
  chapterId: z.number().int().positive('Valid Chapter ID required'),
  health: z.number().int().positive('Health must be > 0'),
  loyal: z.boolean().optional(),
  category: z
    .enum(['AGGRESSOR', 'INCEPTOR', 'TACTICAL', 'CHAPLAIN', 'APOTHECARY'])
    .nullable()
    .optional(),
  weaponType: z.enum([
    'BOLTGUN',
    'HEAVY_BOLTGUN',
    'FLAMER',
    'HEAVY_FLAMER',
    'MULTI_MELTA',
  ]),
});

export type SpaceMarineCreateRequestScheme = z.infer<typeof spaceMarineSchema>;
