package pos.pckg.misc;

public class DataBridgeDirectory {
    private static final String ROOT = "etc/";
    private static final String LOADER = ROOT+"loader/";
    private static final String STATUS = ROOT+"status/";
    public enum  FILES {
        //UNDER LOAD FOLDER
        LOAD_SL_ALL_ACTION,
        LOAD_SL_TYPE,
        LOAD_SL_USERS,
        LOAD_TL_TYPE,

        //UNDER STATUS FOLDER
        RFID_DEVICE_SIGNAL,
        RFID_GSM_SIGNAL,
        RFID_GSM_STATUS,

        //UNDER THE ROOT DIRECTORY
        CACHE_ADMIN_SELECTED_USER,
        CACHE_CARD_INFO,
        CACHE_CHECKOUT_CARD,
        CACHE_CHECKOUT_CUSTOMER,
        CACHE_CHECKOUT_TOTAL,
        CACHE_MESSAGE,
        CACHE_NEW_ACCOUNT,
        CACHE_OTHERS,
        CACHE_RELOAD_CARD,
        CACHE_RELOAD_CUSTOMER,
        CACHE_SECONDARY_CHECK_CARD,
        CACHE_SECONDARY_STATUS,
        CACHE_SECONDARY_TABLE,
        CACHE_SELECTED_CUSTOMER,
        CACHE_SELECTED_ITEM,
        CACHE_SL_VIEW,
        CACHE_TL_VIEW,
        CACHE_USER,


        CONNECTION,
        RFID_CACHE,
        INITIAL
    };

    public static String getDirectory(DataBridgeDirectory.FILES files){
        switch (files){
            case LOAD_SL_TYPE:
                return LOADER+"load-sl-type.file";
            case LOAD_SL_USERS:
                return LOADER+"load-sl-users.file";
            case LOAD_TL_TYPE:
                return LOADER+"load-tl-type.file";
            case LOAD_SL_ALL_ACTION:
                return LOADER+"load-sl-all-action.file";

            case RFID_DEVICE_SIGNAL:
                return STATUS+"rfid-device-signal.file";
            case RFID_GSM_SIGNAL:
                return STATUS+"rfid-gsm-signal.file";
            case RFID_GSM_STATUS:
                return STATUS+"rfid-gsm-status.file";


            case CACHE_ADMIN_SELECTED_USER:
                return ROOT+"cache-admin-selected-user.file";
            case CACHE_CARD_INFO:
                return ROOT+"cache-card-info.file";
            case CACHE_CHECKOUT_CARD:
                return ROOT+"cache-checkout-card.file";
            case CACHE_CHECKOUT_CUSTOMER:
                return ROOT+"cache-checkout-customer.file";
            case CACHE_CHECKOUT_TOTAL:
                return ROOT+"cache-checkout-total.file";
            case CACHE_MESSAGE:
                return ROOT+"cache-message.file";
            case CACHE_NEW_ACCOUNT:
                return ROOT+"cache-new-account.file";
            case CACHE_OTHERS:
                return ROOT+"cache-others.file";
            case CACHE_RELOAD_CARD:
                return ROOT+"cache-reload-card.file";
            case CACHE_RELOAD_CUSTOMER:
                return ROOT+"cache-reload-customer.file";
            case CACHE_SECONDARY_CHECK_CARD:
                return ROOT+"cache-secondary-check-card.file";
            case CACHE_SECONDARY_STATUS:
                return ROOT+"cache-secondary-status.file";
            case CACHE_SECONDARY_TABLE:
                return ROOT+"cache-secondary-table.file";
            case CACHE_SELECTED_CUSTOMER:
                return ROOT+"cache-selected-customer.file";
            case CACHE_SELECTED_ITEM:
                return ROOT+"cache-selected-item.file";
            case CACHE_SL_VIEW:
                return ROOT+"cache-sl-view.file";
            case CACHE_TL_VIEW:
                return ROOT+"cache-tl-view.file";
            case CACHE_USER:
                return ROOT+"cache-user.file";


            case INITIAL:
                return ROOT+"initial.file";
            case CONNECTION:
                return ROOT+"Connection.properties";
            case RFID_CACHE:
                return ROOT+"rfid-cache.file";

            default:return null;
        }

    }

    public static String[] getAllLocalDataBridge(){
        return new String[]{
                getDirectory(FILES.LOAD_SL_ALL_ACTION),
                getDirectory(FILES.LOAD_SL_TYPE),
                getDirectory(FILES.LOAD_SL_USERS),
                getDirectory(FILES.LOAD_TL_TYPE),

                getDirectory(FILES.RFID_DEVICE_SIGNAL),
                getDirectory(FILES.RFID_GSM_SIGNAL),
                getDirectory(FILES.RFID_GSM_STATUS),

                getDirectory(FILES.CACHE_ADMIN_SELECTED_USER),
                getDirectory(FILES.CACHE_CARD_INFO),
                getDirectory(FILES.CACHE_CHECKOUT_CARD),
                getDirectory(FILES.CACHE_CHECKOUT_CUSTOMER),
                getDirectory(FILES.CACHE_CHECKOUT_TOTAL),
                getDirectory(FILES.CACHE_MESSAGE),
                getDirectory(FILES.CACHE_NEW_ACCOUNT),
                getDirectory(FILES.CACHE_OTHERS),
                getDirectory(FILES.CACHE_RELOAD_CARD),
                getDirectory(FILES.CACHE_RELOAD_CUSTOMER),
                getDirectory(FILES.CACHE_SECONDARY_CHECK_CARD),
                getDirectory(FILES.CACHE_SECONDARY_STATUS),
                getDirectory(FILES.CACHE_SECONDARY_TABLE),
                getDirectory(FILES.CACHE_SELECTED_CUSTOMER),
                getDirectory(FILES.CACHE_SELECTED_ITEM),
                getDirectory(FILES.CACHE_SL_VIEW),
                getDirectory(FILES.CACHE_TL_VIEW),
                getDirectory(FILES.CACHE_USER),

                getDirectory(FILES.CONNECTION),
                getDirectory(FILES.INITIAL),
                getDirectory(FILES.RFID_CACHE),
        };
    }
}
