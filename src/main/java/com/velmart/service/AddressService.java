package com.velmart.service;

import com.velmart.dto.request.AddressRequest;
import com.velmart.exception.ResourceNotFoundException;
import com.velmart.exception.UnauthorizedActionException;
import com.velmart.model.Address;
import com.velmart.repository.AddressRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AddressService {

    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public List<Address> getUserAddresses(String userId) {
        return addressRepository.findByUserId(userId);
    }

    public Address getAddressById(String userId, String addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));
        if (!address.getUserId().equals(userId)) {
            throw new UnauthorizedActionException("Access denied. You do not own this address.");
        }
        return address;
    }

    public Address createAddress(String userId, AddressRequest request) {
        List<Address> existing = addressRepository.findByUserId(userId);
        boolean isFirst = existing.isEmpty();
        boolean shouldBeDefault = Boolean.TRUE.equals(request.getIsDefault()) || isFirst;

        if (shouldBeDefault) {
            addressRepository.clearDefaultAddresses(userId);
        }

        Address address = new Address();
        address.setAddressId(UUID.randomUUID().toString());
        address.setUserId(userId);
        address.setRecipientName(request.getRecipientName().trim());
        address.setPhone(request.getPhone().trim());
        address.setAddressLine1(request.getAddressLine1().trim());
        address.setAddressLine2(request.getAddressLine2() != null ? request.getAddressLine2().trim() : "");
        address.setCity(request.getCity().trim());
        address.setState(request.getState().trim());
        address.setPostalCode(request.getPostalCode().trim());
        address.setCountry(request.getCountry() != null ? request.getCountry().trim() : "India");
        address.setIsDefault(shouldBeDefault);
        address.setCreatedAt(Instant.now().toString());

        return addressRepository.save(address);
    }

    public Address updateAddress(String userId, String addressId, AddressRequest request) {
        Address address = getAddressById(userId, addressId);

        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(address.getIsDefault())) {
            addressRepository.clearDefaultAddresses(userId);
            address.setIsDefault(true);
        } else if (request.getIsDefault() != null) {
            address.setIsDefault(request.getIsDefault());
        }

        address.setRecipientName(request.getRecipientName().trim());
        address.setPhone(request.getPhone().trim());
        address.setAddressLine1(request.getAddressLine1().trim());
        address.setAddressLine2(request.getAddressLine2() != null ? request.getAddressLine2().trim() : "");
        address.setCity(request.getCity().trim());
        address.setState(request.getState().trim());
        address.setPostalCode(request.getPostalCode().trim());
        address.setCountry(request.getCountry() != null ? request.getCountry().trim() : "India");

        return addressRepository.save(address);
    }

    public void setDefaultAddress(String userId, String addressId) {
        Address address = getAddressById(userId, addressId);
        addressRepository.clearDefaultAddresses(userId);
        address.setIsDefault(true);
        addressRepository.save(address);
    }

    public void deleteAddress(String userId, String addressId) {
        Address address = getAddressById(userId, addressId);
        addressRepository.delete(addressId);
    }
}
