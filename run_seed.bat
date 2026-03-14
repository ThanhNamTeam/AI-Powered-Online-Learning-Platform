@echo off
REM ============================================================
REM run_seed.bat — Chạy seed data cho SWD392
REM DB: swd392_db | PostgreSQL @ localhost:5432
REM ============================================================
REM Cách dùng:
REM   run_seed.bat          -> chạy seed_data.sql (admin dashboard)
REM   run_seed.bat ins      -> chạy seed_instructor.sql (instructor@gmail.com)
REM   run_seed.bat check    -> chạy seed_modules.sql (kiểm tra module count)
REM   run_seed.bat all      -> chạy cả 3 file theo thứ tự
REM ============================================================

SET PGPASSWORD=admin123
SET PSQL=psql -h localhost -p 5432 -U postgres -d swd392_db

IF "%1"=="ins" (
    echo [SEED] Chay seed_instructor.sql...
    %PSQL% -f seed_instructor.sql
    echo [DONE] seed_instructor.sql hoan thanh.
    GOTO :EOF
)

IF "%1"=="check" (
    echo [CHECK] Kiem tra module count...
    %PSQL% -f seed_modules.sql
    GOTO :EOF
)

IF "%1"=="all" (
    echo [SEED] Chay seed_data.sql...
    %PSQL% -f seed_data.sql
    echo [SEED] Chay seed_instructor.sql...
    %PSQL% -f seed_instructor.sql
    echo [CHECK] Kiem tra tong ket...
    %PSQL% -f seed_modules.sql
    echo [DONE] Tat ca seed da chay xong!
    GOTO :EOF
)

REM Default: seed_data.sql
echo [SEED] Chay seed_data.sql (Admin Dashboard)...
%PSQL% -f seed_data.sql
echo [DONE] seed_data.sql hoan thanh.
