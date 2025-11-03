ENV=$1

case "$ENV" in
    dev)
        echo "3001"
        ;;
    qa)
        echo "3002"
        ;;
    staging)
        echo "3003"
        ;;
    prod)
        echo "3000"
        ;;
    *)
        echo "8080"
        ;;
esac
