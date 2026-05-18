-- AES-GCM encrypted base64 strings are longer than plaintext PAN/Aadhar/UAN/ESI/bank-account.
-- Widen the columns so historic plaintext + new ciphertext both fit.
ALTER TABLE employees
    ALTER COLUMN pan_number          TYPE VARCHAR(200),
    ALTER COLUMN aadhar_number       TYPE VARCHAR(200),
    ALTER COLUMN uan_number          TYPE VARCHAR(200),
    ALTER COLUMN esi_number          TYPE VARCHAR(200),
    ALTER COLUMN bank_account_number TYPE VARCHAR(200);
