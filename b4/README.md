B4 donor module. Copy src/ into the project (base package assumed com.bloodbank), overwriting Donor.java and DonorRepository.java from B2.
Assumed from earlier sessions (adapt imports if different):
- com.bloodbank.common.dto.PageResponse.from(Page<T>)
- com.bloodbank.common.exception.NotFoundException(String), BusinessException(String code, String message)
- User has getEmail() and isActive(); authorities are ROLE_ADMIN etc.
- donor table has columns: full_name, phone, date_of_birth, weight_kg, city, blood_group, last_donation_date, deferred_until, deferral_reason
