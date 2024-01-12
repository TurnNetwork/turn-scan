package com.turn.browser.cache;

import com.turn.browser.dao.entity.Address;
import com.turn.browser.elasticsearch.dto.Transaction;
import com.turn.browser.enums.AddressTypeEnum;
import com.turn.browser.enums.ContractDescEnum;
import com.turn.browser.enums.ContractTypeEnum;
import com.turn.browser.enums.InnerContractAddrEnum;
import com.turn.browser.v0152.analyzer.ErcCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Address statistics cache, use with caution
 * When block A enters CollectionEventHandler from BlockEventHandler, and before it has time to clear the cache, block A+1 will read dirty data when it enters BlockEventHandler or CollectionEventHandler.
 *
 */
@Slf4j
@Component
public class AddressCache {

    @Resource
    private ErcCache ercCache;

    //Current address cache. This cache will be cleared after StatisticsAddressConverter executes the business logic.
    // So this is the current cumulative address cache from the last time the StatisticsAddressConverter business was executed, not all
    private Map<String, Address> addressMap = new ConcurrentHashMap<>();

    // Full EVM contract address cache
    private Set<String> evmContractAddressCache = new HashSet<>();

    public Set<String> getEvmContractAddressCache() {
        return this.evmContractAddressCache;
    }

    public boolean isEvmContractAddress(String address) {
        return this.evmContractAddressCache.contains(address);
    }

    public boolean isErc20ContractAddress(String address) {
        return ercCache.getErc20AddressCache().contains(address);
    }

    public boolean isErc721ContractAddress(String address) {
        return ercCache.getErc721AddressCache().contains(address);
    }

    public boolean isErc1155ContractAddress(String address) {
        return ercCache.getErc1155AddressCache().contains(address);
    }

    // Full WASM contract address cache
    private Set<String> wasmContractAddressCache = new HashSet<>();

    public Set<String> getWasmContractAddressCache() {
        return this.wasmContractAddressCache;
    }

    public boolean isWasmContractAddress(String address) {
        return this.wasmContractAddressCache.contains(address);
    }

    public Integer getTypeData(String address) {
        if (InnerContractAddrEnum.getAddresses().contains(address)) return Transaction.ToTypeEnum.INNER_CONTRACT.getCode();
        if (this.isEvmContractAddress(address)) return Transaction.ToTypeEnum.EVM_CONTRACT.getCode();
        if (this.isWasmContractAddress(address)) return Transaction.ToTypeEnum.WASM_CONTRACT.getCode();
        if (isErc20ContractAddress(address)) return Transaction.ToTypeEnum.ERC20_CONTRACT.getCode();
        if (isErc721ContractAddress(address)) return Transaction.ToTypeEnum.ERC721_CONTRACT.getCode();
        if (isErc1155ContractAddress(address)) return Transaction.ToTypeEnum.ERC1155_CONTRACT.getCode();
        return Transaction.ToTypeEnum.ACCOUNT.getCode();
    }

    public void update(Transaction tx) {
        String from = tx.getFrom();
        String to = tx.getTo();
        String contractAddress = tx.getContractAddress();
        /**
         * The from information created by the contract cannot be overwritten to the contract account type.
         */
        switch (tx.getTypeEnum()) {
            case EVM_CONTRACT_CREATE:
            case WASM_CONTRACT_CREATE:
            case ERC20_CONTRACT_CREATE:
            case ERC721_CONTRACT_CREATE:
            case ERC1155_CONTRACT_CREATE:
                this.updateContractFromAddress(from);
                this.updateAddress(tx, contractAddress);
                break;
            default:
                this.updateAddress(tx, from);
                this.updateAddress(tx, to);
                break;
        }
    }

    // Initialize the contract map first to prevent subsequent contract transactions from finding the corresponding contract and causing statistical errors.
    public void updateFirst(String addr, ContractTypeEnum contractTypeEnum) {
        Address address = this.addressMap.get(addr);
        if (address == null) {
            address = this.createDefaultAddress(addr);
            switch (contractTypeEnum) {
                case EVM:
                    address.setType(AddressTypeEnum.EVM_CONTRACT.getCode());
                    this.evmContractAddressCache.add(addr);
                    break;
                case WASM:
                    address.setType(AddressTypeEnum.WASM_CONTRACT.getCode());
                    this.wasmContractAddressCache.add(addr);
                    break;
                case ERC20_EVM:
                    address.setType(AddressTypeEnum.ERC20_EVM_CONTRACT.getCode());
                    break;
                case ERC721_EVM:
                    address.setType(AddressTypeEnum.ERC721_EVM_CONTRACT.getCode());
                    break;
                case ERC1155_EVM:
                    address.setType(AddressTypeEnum.ERC1155_EVM_CONTRACT.getCode());
                    break;
                case GAME_EVM:
                    address.setType(AddressTypeEnum.GAME_EVM_CONTRACT.getCode());
                    break;
                default:
                    break;
            }
            this.addressMap.put(addr, address);
        }
    }

    public Collection<Address> getAll() {
        return this.addressMap.values();
    }

    public void cleanAll() {
        this.addressMap.clear();
    }

    public void updateAddress(Transaction tx, String addr) {
        if (addr == null) return;
        Address address = this.addressMap.get(addr);
        if (address == null) {
            address = this.createDefaultAddress(addr);
            this.addressMap.put(addr, address);
        }
        switch (tx.getTypeEnum()) {
            case TRANSFER: // Transfer transaction
                break;
            case STAKE_CREATE:// Create a validator
            case STAKE_INCREASE:// Add own pledge
            case STAKE_MODIFY:// edit validator
            case STAKE_EXIT:// Exit the validator
            case REPORT:// Report the verifier
                break;
            case DELEGATE_CREATE:// initiates delegation
            case DELEGATE_EXIT:// cancel the delegation
            case CLAIM_REWARDS:// Receive commission rewards
                break;
            case PROPOSAL_TEXT:// creates a text proposal
            case PROPOSAL_UPGRADE:// Create an upgrade proposal
            case PROPOSAL_PARAMETER:// Create parameter proposal
            case PROPOSAL_VOTE:// proposal voting
            case PROPOSAL_CANCEL:// Cancel proposal
            case VERSION_DECLARE:// version declaration
                break;
            case EVM_CONTRACT_CREATE:
                // If the address is the contract address returned in the receipt created by the EVM contract
                address.setContractCreatehash(tx.getHash());
                address.setContractCreate(tx.getFrom());
                // Override the value set in createDefaultAddress()
                address.setType(AddressTypeEnum.EVM_CONTRACT.getCode());
                this.evmContractAddressCache.add(addr);
                address.setContractBin(tx.getBin());
                break;
            case WASM_CONTRACT_CREATE:
                // If the address is the contract address returned in the receipt created by the WASM contract
                address.setContractCreatehash(tx.getHash());
                address.setContractCreate(tx.getFrom());
                // Override the value set in createDefaultAddress()
                address.setType(AddressTypeEnum.WASM_CONTRACT.getCode());
                this.wasmContractAddressCache.add(addr);
                address.setContractBin(tx.getBin());
                break;
            case ERC20_CONTRACT_CREATE:
                // If the address is the contract address returned in the receipt created by the EVM contract
                address.setContractCreatehash(tx.getHash());
                address.setContractCreate(tx.getFrom());
                // Override the value set in createDefaultAddress()
                address.setType(AddressTypeEnum.ERC20_EVM_CONTRACT.getCode());
                address.setContractBin(tx.getBin());
                break;
            case ERC721_CONTRACT_CREATE:
                // If the address is the contract address returned in the receipt created by the EVM contract
                address.setContractCreatehash(tx.getHash());
                address.setContractCreate(tx.getFrom());
                // Override the value set in createDefaultAddress()
                address.setType(AddressTypeEnum.ERC721_EVM_CONTRACT.getCode());
                address.setContractBin(tx.getBin());
                break;
            case ERC1155_CONTRACT_CREATE:
                // If the address is the contract address returned in the receipt created by the EVM contract
                address.setContractCreatehash(tx.getHash());
                address.setContractCreate(tx.getFrom());
                // Override the value set in createDefaultAddress()
                address.setType(AddressTypeEnum.ERC1155_EVM_CONTRACT.getCode());
                address.setContractBin(tx.getBin());
                break;
            default:
        }
    }

    private void updateContractFromAddress(String addr) {
        if (addr == null) return;
        Address address = this.addressMap.get(addr);
        if (address == null) {
            address = this.createDefaultAddress(addr);
            this.addressMap.put(addr, address);
        }
    }

    /**
     * Create a default address
     *
     * @param addr:
     * @return: com.turn.browser.dao.entity.Address
     */
    public Address createDefaultAddress(String addr) {
        Address address = new Address();
        address.setAddress(addr);
        //Set address type
        if (InnerContractAddrEnum.getAddresses().contains(addr)) {
            // Built-in contract address
            address.setType(AddressTypeEnum.INNER_CONTRACT.getCode());
        } else {
            // Set it to the account address by default first. The specific type is determined and set by the subsequent logic of calling this method.
            address.setType(AddressTypeEnum.ACCOUNT.getCode());
        }

        ContractDescEnum cde = ContractDescEnum.getMap().get(addr);
        if (cde != null) {
            address.setContractName(cde.getContractName());
            address.setContractCreate(cde.getCreator());
            address.setContractCreatehash(cde.getContractHash());
        } else {
            address.setContractName("");
            address.setContractCreate("");
            address.setContractCreatehash("");
        }

        address.setTxQty(0);
        address.setErc20TxQty(0);
        address.setErc721TxQty(0);
        address.setErc1155TxQty(0);
        address.setTransferQty(0);
        address.setStakingQty(0);
        address.setDelegateQty(0);
        address.setProposalQty(0);
        address.setHaveReward(BigDecimal.ZERO);
        return address;
    }

    /**
     * Initialize EVM address cache
     *
     * @param addressList address entity list
     */
    public void initEvmContractAddressCache(List<Address> addressList) {
        if (addressList.isEmpty()) return;
        this.evmContractAddressCache.clear();
        addressList.forEach(address -> {
            if (address.getType() == AddressTypeEnum.EVM_CONTRACT.getCode()) {
                this.evmContractAddressCache.add(address.getAddress());
            }
        });
    }

    /**
     * Initialize WASM address cache
     *
     * @param addressList address entity list
     */
    public void initWasmContractAddressCache(List<Address> addressList) {
        if (addressList.isEmpty()) return;
        this.wasmContractAddressCache.clear();
        addressList.forEach(address -> {
            if (address.getType() == AddressTypeEnum.WASM_CONTRACT.getCode()) {
                this.wasmContractAddressCache.add(address.getAddress());
            }
        });
    }

    /**
     * Initialize the built-in address and initialize it for the first time
     */
    public void initOnFirstStart() {
        log.info("Initialize built-in address");
        for (ContractDescEnum contractDescEnum : ContractDescEnum.values()) {
            this.addressMap.put(contractDescEnum.getAddress(), this.createDefaultAddress(contractDescEnum.getAddress()));
        }
    }

    /**
     * Get the cache address object
     *
     * @param address address
     * @return: com.turn.browser.dao.entity.Address
     */
    public Address getAddress(String address) {
        Address cache = this.addressMap.get(address);
        return cache;
    }

    /**
     * Add address
     *
     * @param address:
     * @return: void
     */
    public void addAddress(Address address) {
        this.addressMap.put(address.getAddress(), address);
    }

}
