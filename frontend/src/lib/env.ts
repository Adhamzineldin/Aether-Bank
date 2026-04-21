interface AppEnv {
  API_URL: string;
  IS_DEV: boolean;
}

export const env: AppEnv = {
  API_URL: import.meta.env.VITE_API_URL || '',
  IS_DEV: import.meta.env.DEV,
};

